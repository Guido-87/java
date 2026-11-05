package com.spring.ia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ia.client.CodexAppServerClient;
import com.spring.ia.dto.*;
import com.spring.ia.util.CodexAgentEventMapper;
import com.spring.ia.util.ApprovalPolicy;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class AgentTaskService {
    private static final Logger logger = LoggerFactory.getLogger(AgentTaskService.class);
    private static final List<String> ROLES = List.of("enzo_seguridad", "rucau_arquitecto", "jona_dev1", "jesse_dev2", "nahitan_devops", "juli_qa");
    private static final int MAX_RETAINED_TASKS = 256;
    private static final int MAX_SSE_SUBSCRIBERS = 4;
    private final boolean enabled;
    private final CodexAppServerClient client;
    private final CodexAgentEventMapper eventMapper = new CodexAgentEventMapper();
    private final String approvalMode;
    private final Path workspace;
    private final long taskTimeoutSeconds;
    private final long startupTimeoutSeconds;
    private final ScheduledExecutorService timeoutExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "agent-task-timeouts"); thread.setDaemon(true); return thread;
    });
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();
    private final AtomicReference<String> activeTask = new AtomicReference<>();

    public AgentTaskService(@Value("${codex.agent.enabled:false}") boolean enabled,
            @Value("${codex.agent.approval-mode:manual}") String approvalMode,
            @Value("${codex.agent.workspace-root:${user.dir}}") String workspace,
            @Value("${codex.agent.task-timeout-seconds:1800}") long taskTimeoutSeconds,
            @Value("${codex.agent.startup-timeout-seconds:30}") long startupTimeoutSeconds,
            CodexAppServerClient client) {
        this.enabled = enabled; this.approvalMode = "automatic".equalsIgnoreCase(approvalMode) ? "automatic" : "manual";
        this.workspace = Path.of(workspace).toAbsolutePath().normalize(); this.taskTimeoutSeconds = Math.max(1, taskTimeoutSeconds);
        this.startupTimeoutSeconds = Math.max(1, startupTimeoutSeconds); this.client = client;
    }
    public AgentTaskStatusResponse status(String session) {
        Task task = tasks.get(session);
        return new AgentTaskStatusResponse(enabled, !enabled ? "disabled" : client.isRunning() ? "connected" : "disconnected", approvalMode, task == null ? null : task.snapshot(), task == null ? initialAgents(AgentStatus.AVAILABLE) : task.agents());
    }
    public AgentTaskSnapshot submit(String session, String prompt, String requestedApprovalMode) {
        if (!enabled) throw new IllegalStateException("Local agent orchestration is disabled");
        if (!activeTask.compareAndSet(null, session)) throw new IllegalStateException("Another task is active");
        pruneTaskHistory();
        if (tasks.size() >= MAX_RETAINED_TASKS) { activeTask.compareAndSet(session, null); throw new IllegalStateException("Se alcanzó el límite del historial de tareas"); }
        String taskApprovalMode = requestedApprovalMode == null ? approvalMode : requestedApprovalMode;
        if (!"manual".equals(taskApprovalMode) && !"automatic".equals(taskApprovalMode)) {
            activeTask.compareAndSet(session, null); throw new IllegalArgumentException("El modo de aprobación no es válido");
        }
        Task task = new Task(UUID.randomUUID().toString(), session, taskApprovalMode);
        tasks.put(session, task);
        task.update(AgentTaskStatus.QUEUED, "Tarea en cola", null);
        try {
            client.start(event -> onEvent(task, event), approval -> onApproval(task, approval));
            task.startupTimeout = timeoutExecutor.schedule(() -> startupTimeout(task), startupTimeoutSeconds, TimeUnit.SECONDS);
            client.startThread(prompt);
            task.timeout = timeoutExecutor.schedule(() -> timeout(task), taskTimeoutSeconds, TimeUnit.SECONDS);
        } catch (IOException | RuntimeException ex) {
            logger.error("Could not start local Codex task ({}): {}", ex.getClass().getSimpleName(), safeLogMessage(ex.getMessage()));
            task.update(AgentTaskStatus.FAILED, "No se pudo iniciar el servidor local de Codex", null);
            activeTask.compareAndSet(session, null);
            client.stop();
        }
        return task.snapshot();
    }
    public AgentTaskSnapshot get(String session, String taskId) { return owned(session, taskId).snapshot(); }
    public SseEmitter events(String session, String taskId) {
        Task task = owned(session, taskId);
        SseEmitter emitter = new SseEmitter(0L);
        task.add(emitter);
        emitter.onCompletion(() -> task.remove(emitter));
        emitter.onTimeout(() -> task.remove(emitter));
        emitter.onError(error -> task.remove(emitter));
        try { emitter.send(SseEmitter.event().name("snapshot").data(task.snapshot())); }
        catch (IOException e) { disconnect(task, emitter, e); }
        if (isTerminal(task.status)) { task.remove(emitter); emitter.complete(); }
        return emitter;
    }
    public AgentTaskSnapshot approve(String session, String taskId, String approvalId, String decision) throws IOException {
        Task task = owned(session, taskId);
        if (!"accept".equals(decision) && !"decline".equals(decision)) throw new IllegalArgumentException("decision must be accept or decline");
        if (task.status != AgentTaskStatus.WAITING_APPROVAL || task.approval == null || !approvalId.equals(task.approval.id()))
            throw new IllegalArgumentException("Approval is no longer pending");
        if ("accept".equals(decision) && !task.approval.canAccept())
            throw new IllegalArgumentException("No están disponibles los detalles de aprobación; rechazá la solicitud");
        try {
            client.decide(approvalId, decision);
            task.update(AgentTaskStatus.WORKING, "Respuesta de aprobación enviada", null);
            return task.snapshot();
        } catch (IOException ex) {
            logger.error("Could not deliver approval decision ({}): {}", ex.getClass().getSimpleName(), safeLogMessage(ex.getMessage()));
            task.update(AgentTaskStatus.DISCONNECTED, "No se pudo confirmar si Codex recibió la decisión; verificá el estado de la tarea", null);
            activeTask.compareAndSet(session, null);
            client.stop();
            throw ex;
        } catch (IllegalArgumentException ex) {
            task.update(AgentTaskStatus.DISCONNECTED, "La solicitud de aprobación ya no está pendiente; verificá el estado de la tarea", null);
            activeTask.compareAndSet(session, null);
            throw ex;
        }
    }
    public AgentTaskSnapshot interrupt(String session, String taskId) throws IOException {
        Task task = owned(session, taskId); client.interrupt(); task.update(AgentTaskStatus.INTERRUPTED, "Tarea interrumpida", null); client.stop(); activeTask.compareAndSet(session, null); return task.snapshot();
    }
    private Task owned(String session, String taskId) {
        Task task = tasks.get(session);
        if (task == null || !task.id.equals(taskId)) throw new IllegalArgumentException("No se encontró la tarea para esta sesión");
        return task;
    }
    private void onApproval(Task task, JsonNode request) {
        String rpcId = request.path("id").asText(); String method = request.path("method").asText();
        JsonNode fileChangeItem = cachedFileChange(task, request.path("params"));
        if (ApprovalPolicy.isWorkspaceFileChange(request, workspace, fileChangeItem)) {
            try { client.decide(rpcId, "accept"); task.update(AgentTaskStatus.WORKING, "Cambio de archivo en el worktree aprobado automáticamente", null); }
            catch (IOException ignored) { task.update(AgentTaskStatus.WAITING_APPROVAL, "Falló la aprobación automática; se requiere aprobación manual", manualApproval(task, request, rpcId, method)); }
            return;
        }
        if ("automatic".equals(task.approvalMode) && ApprovalPolicy.isAutomaticallyApprovable(request, workspace)) {
            try { client.decide(rpcId, "accept"); task.update(AgentTaskStatus.WORKING, "Acción de solo lectura y bajo riesgo aprobada", null); }
            catch (IOException ignored) { task.update(AgentTaskStatus.WAITING_APPROVAL, "Falló la aprobación automática; se requiere aprobación manual", manualApproval(task, request, rpcId, method)); }
            return;
        }
        task.update(AgentTaskStatus.WAITING_APPROVAL, "Se requiere aprobación", manualApproval(task, request, rpcId, method));
    }
    private AgentApproval manualApproval(Task task, JsonNode request, String rpcId, String method) {
        String kind = method.toLowerCase().contains("file") ? "file_change" : "command";
        JsonNode params = request.path("params");
        String title = kind.equals("file_change") ? "Se requiere aprobación para cambiar archivos" : "Se requiere aprobación para ejecutar un comando";
        String details;
        if ("file_change".equals(kind)) {
            JsonNode fileChanges = params.path("fileChanges");
            if (!fileChanges.isMissingNode() && !fileChanges.isNull()) details = fileChanges.toString();
            else {
                JsonNode item = cachedFileChange(task, params);
                details = item == null ? "" : item.path("changes").toString();
            }
        } else {
            JsonNode command = params.path("command");
            details = command.isArray() ? command.toString() : command.asText("");
            JsonNode actions = params.path("commandActions");
            if (details.isBlank() && actions.isArray() && !actions.isEmpty()) details = "Acciones propuestas: " + actions;
        }
        String cwd = params.path("cwd").asText("");
        boolean configuredCwdFallback = cwd.isBlank() && "command".equals(kind) && !details.isBlank();
        if (configuredCwdFallback) cwd = workspace.toString();
        if (!cwd.isBlank()) details += "\nDirectorio de trabajo: " + cwd;
        String reason = params.path("reason").asText("");
        if (configuredCwdFallback) details += "\nNota: Codex no informó la carpeta; se muestra la carpeta configurada para esta tarea.";
        if (!reason.isBlank() && "command".equals(kind)) details += "\nMotivo: " + reason;
        return new AgentApproval(rpcId, kind, title, details);
    }
    private String fileChangeKey(String threadId, String itemId) { return threadId + ":" + itemId; }
    private JsonNode cachedFileChange(Task task, JsonNode params) {
        String threadId = params.path("threadId").asText("");
        String itemId = params.path("itemId").asText("");
        if (threadId.isBlank() || itemId.isBlank()) return null;
        return task.fileChanges.get(fileChangeKey(threadId, itemId));
    }
    private void onEvent(Task task, JsonNode event) {
        String incomingMethod = event.path("method").asText();
        if ("app-server/thread-start-failed".equals(incomingMethod) || "app-server/turn-start-failed".equals(incomingMethod)) {
            task.update(AgentTaskStatus.FAILED, "No se pudo iniciar la tarea de Codex", null);
            activeTask.compareAndSet(task.session, null);
            client.stop();
            return;
        }
        if ("app-server/disconnected".equals(event.path("method").asText())) {
            if (task.status != AgentTaskStatus.COMPLETED && task.status != AgentTaskStatus.FAILED && task.status != AgentTaskStatus.INTERRUPTED)
                task.update(AgentTaskStatus.DISCONNECTED, "Se desconectó el servidor local de Codex", null);
            activeTask.compareAndSet(task.session, null); return;
        }
        String method = event.path("method").asText();
        rememberFileChange(task, method, event.path("params"));
        JsonNode parentThreadId = event.path("params").path("thread").path("parentThreadId");
        if ("thread/started".equals(method) && (!parentThreadId.isTextual() || parentThreadId.asText().isBlank())) {
            task.rootThread = event.path("params").path("thread").path("id").asText();
            task.cancelStartupTimeout();
            if (task.status == AgentTaskStatus.QUEUED) task.update(AgentTaskStatus.WORKING, "Tarea en curso", null);
        }
        var update = task.rootThread == null ? null : eventMapper.map(event, task.rootThread);
        if (update == null) { task.publish(); return; }
        update.agentStatuses().forEach(task.agentStatuses::put);
        if (update.response() != null) task.response = update.response();
        if (!update.agentStatuses().isEmpty() && update.taskStatus() == null) {
            task.summary = agentActivitySummary(task.agentStatuses);
        }
        if (update.taskStatus() != null) {
            task.update(update.taskStatus(), switch (update.taskStatus()) { case COMPLETED -> "Tarea finalizada"; case FAILED -> "La tarea falló"; case INTERRUPTED -> "Tarea interrumpida"; default -> "Tarea en curso"; }, null);
            if (update.taskStatus() == AgentTaskStatus.COMPLETED || update.taskStatus() == AgentTaskStatus.FAILED || update.taskStatus() == AgentTaskStatus.INTERRUPTED) { activeTask.compareAndSet(task.session, null); client.stop(); }
        } else task.publish();
    }
    private void rememberFileChange(Task task, String method, JsonNode params) {
        if (!"item/started".equals(method)) return;
        JsonNode item = params.path("item");
        if (!"fileChange".equals(item.path("type").asText()) || !item.path("changes").isArray()) return;
        String threadId = params.path("threadId").asText("");
        String itemId = item.path("id").asText("");
        if (threadId.isBlank() || itemId.isBlank() || item.path("changes").isEmpty()) return;
        String key = fileChangeKey(threadId, itemId);
        if (task.fileChanges.size() >= 32 && !task.fileChanges.containsKey(key)) {
            task.fileChanges.keySet().stream().findFirst().ifPresent(task.fileChanges::remove);
        }
        task.fileChanges.put(key, item.deepCopy());
    }
    private List<AgentState> initialAgents(AgentStatus status) { return ROLES.stream().map(id -> new AgentState(id, status)).toList(); }
    private String agentActivitySummary(Map<String, AgentStatus> statuses) {
        List<String> active = ROLES.stream()
                .filter(role -> statuses.get(role) == AgentStatus.WORKING || statuses.get(role) == AgentStatus.WAITING_APPROVAL)
                .map(role -> switch (role) {
                    case "enzo_seguridad" -> "Enzo";
                    case "rucau_arquitecto" -> "Rucau";
                    case "jona_dev1" -> "Jona";
                    case "jesse_dev2" -> "Jesse";
                    case "nahitan_devops" -> "Nahitan";
                    case "juli_qa" -> "Juli";
                    default -> role;
                })
                .toList();
        return active.isEmpty() ? "Se actualizó el estado de los agentes" : "Trabajando: " + String.join(", ", active);
    }
    private void timeout(Task task) {
        if (!isTerminal(task.status)) {
            task.update(AgentTaskStatus.FAILED, "La tarea superó el tiempo límite", null);
            activeTask.compareAndSet(task.session, null);
            client.stop();
        }
    }
    private void startupTimeout(Task task) {
        if (task.status == AgentTaskStatus.QUEUED && task.rootThread == null) {
            task.update(AgentTaskStatus.FAILED, "Codex superó el tiempo límite para iniciar la tarea", null);
            activeTask.compareAndSet(task.session, null);
            client.stop();
        }
    }
    private void pruneTaskHistory() {
        long cutoff = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(2);
        tasks.entrySet().removeIf(entry -> isTerminal(entry.getValue().status) && entry.getValue().updatedAt.toEpochMilli() < cutoff);
        while (tasks.size() >= MAX_RETAINED_TASKS) {
            Task oldest = tasks.values().stream().filter(task -> isTerminal(task.status))
                    .min(java.util.Comparator.comparing(task -> task.updatedAt)).orElse(null);
            if (oldest == null) break;
            tasks.remove(oldest.session, oldest);
        }
    }
    private void disconnect(Task task, SseEmitter emitter, IOException error) {
        task.remove(emitter);
        logger.warn("SSE subscriber disconnected ({}): {}", error.getClass().getSimpleName(), safeLogMessage(error.getMessage()));
        emitter.complete();
    }
    private String safeLogMessage(String message) {
        if (message == null || message.isBlank()) return "No error message provided";
        String safe = message.replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("(?i)(authorization|api[-_ ]?key|access[-_ ]?token|password)(\\s*[:=]\\s*)\\S+", "$1$2[REDACTED]")
                .replaceAll("(?i)Bearer\\s+\\S+", "Bearer [REDACTED]");
        return safe.length() > 500 ? safe.substring(0, 500) : safe;
    }
    private boolean isTerminal(AgentTaskStatus status) {
        return status == AgentTaskStatus.COMPLETED || status == AgentTaskStatus.FAILED || status == AgentTaskStatus.INTERRUPTED || status == AgentTaskStatus.DISCONNECTED;
    }
    @PreDestroy public void shutdown() {
        timeoutExecutor.shutdownNow();
        tasks.values().forEach(task -> { task.cancelTimeout(); task.completeEmitters(); });
        client.stop();
    }
    private final class Task {
        final String id; final String session; final String approvalMode; volatile String rootThread; volatile AgentTaskStatus status = AgentTaskStatus.QUEUED;
        volatile String summary = "Tarea en cola"; volatile String response; volatile Instant updatedAt = Instant.now(); volatile AgentApproval approval;
        final Map<String, AgentStatus> agentStatuses = new ConcurrentHashMap<>(); final Map<String, JsonNode> fileChanges = new ConcurrentHashMap<>();
        final List<SseEmitter> emitters = new java.util.concurrent.CopyOnWriteArrayList<>();
        volatile ScheduledFuture<?> timeout; volatile ScheduledFuture<?> startupTimeout;
        Task(String id, String session, String approvalMode) { this.id = id; this.session = session; this.approvalMode = approvalMode; ROLES.forEach(role -> agentStatuses.put(role, AgentStatus.AVAILABLE)); }
        void update(AgentTaskStatus next, String safeSummary, AgentApproval pending) {
            if (isTerminal(next)) {
                fileChanges.clear();
                AgentStatus finalAgentStatus = switch (next) {
                    case COMPLETED -> AgentStatus.COMPLETED;
                    case FAILED -> AgentStatus.FAILED;
                    case INTERRUPTED -> AgentStatus.INTERRUPTED;
                    case DISCONNECTED -> AgentStatus.DISCONNECTED;
                    default -> AgentStatus.AVAILABLE;
                };
                agentStatuses.replaceAll((role, current) -> current == AgentStatus.QUEUED || current == AgentStatus.WORKING || current == AgentStatus.WAITING_APPROVAL
                        ? finalAgentStatus : current);
            }
            status = next; summary = safeSummary; approval = pending; updatedAt = Instant.now(); publish();
            if (isTerminal(next)) { cancelTimeout(); completeEmitters(); }
        }
        List<AgentState> agents() { return ROLES.stream().map(role -> new AgentState(role, agentStatuses.getOrDefault(role, AgentStatus.AVAILABLE))).toList(); }
        AgentTaskSnapshot snapshot() { return new AgentTaskSnapshot(id, status, approvalMode, summary, response, updatedAt, agents(), approval); }
        void add(SseEmitter emitter) {
            if (emitters.size() >= MAX_SSE_SUBSCRIBERS) { emitter.complete(); throw new IllegalStateException("Too many SSE subscribers"); }
            emitters.add(emitter);
        }
        void remove(SseEmitter emitter) { emitters.remove(emitter); }
        void cancelTimeout() {
            ScheduledFuture<?> scheduled = timeout; if (scheduled != null) scheduled.cancel(false);
            cancelStartupTimeout();
        }
        void cancelStartupTimeout() { ScheduledFuture<?> scheduled = startupTimeout; if (scheduled != null) scheduled.cancel(false); }
        void completeEmitters() { emitters.forEach(SseEmitter::complete); emitters.clear(); }
        void publish() { updatedAt = Instant.now(); var value = snapshot(); emitters.forEach(e -> { try { e.send(SseEmitter.event().name("snapshot").data(value)); } catch (IOException ex) { disconnect(this, e, ex); } }); }
    }
}
