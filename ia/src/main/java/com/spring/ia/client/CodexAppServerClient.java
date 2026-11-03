package com.spring.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Owns one local Codex App Server process. JSON-RPC traffic is kept off application logs. */
@Component
public class CodexAppServerClient {
    private static final Logger logger = LoggerFactory.getLogger(CodexAppServerClient.class);
    private final ObjectMapper mapper;
    private final String executable;
    private final Path workspace;
    private final AtomicLong requestIds = new AtomicLong();
    private final Map<String, JsonNode> approvals = new ConcurrentHashMap<>();
    private final Map<String, String> pendingRequests = new ConcurrentHashMap<>();
    private volatile java.util.concurrent.CountDownLatch initialized = new java.util.concurrent.CountDownLatch(1);
    private volatile String initializeError;
    private volatile Process process;
    private volatile BufferedWriter input;
    private final ProcessStarter processStarter;

    @Autowired
    public CodexAppServerClient(@Value("${codex.agent.command:codex}") String executable,
            @Value("${codex.agent.workspace-root:${user.dir}}") String workspace) {
        this(new ObjectMapper(), executable, workspace, ProcessBuilder::start);
    }

    CodexAppServerClient(ObjectMapper mapper, String executable, String workspace, ProcessStarter processStarter) {
        this.mapper = mapper;
        this.executable = executable;
        this.workspace = Path.of(workspace).toAbsolutePath().normalize();
        this.processStarter = processStarter;
    }

    public synchronized boolean isRunning() { return process != null && process.isAlive(); }

    public synchronized void start(Consumer<JsonNode> events, Consumer<JsonNode> approvalRequests) throws IOException {
        if (isRunning()) throw new IllegalStateException("Codex App Server is already running");
        initialized = new java.util.concurrent.CountDownLatch(1);
        initializeError = null;
        requestIds.set(0);
        approvals.clear();
        pendingRequests.clear();
        ProcessBuilder builder = executable.toLowerCase().endsWith(".cmd") || executable.toLowerCase().endsWith(".bat")
                ? new ProcessBuilder("cmd.exe", "/d", "/c", executable, "app-server", "--listen", "stdio://")
                : new ProcessBuilder(executable, "app-server", "--listen", "stdio://");
        builder.directory(workspace.toFile());
        Map<String, String> inherited = Map.copyOf(builder.environment());
        builder.environment().clear();
        for (String key : Set.of("PATH", "Path", "USERPROFILE", "APPDATA", "LOCALAPPDATA", "HOME", "CODEX_HOME", "SYSTEMROOT", "SystemRoot", "TEMP", "TMP")) {
            String value = inherited.get(key);
            if (value != null) builder.environment().put(key, value);
        }
        Process child = processStarter.start(builder);
        process = child;
        input = new BufferedWriter(new OutputStreamWriter(child.getOutputStream(), StandardCharsets.UTF_8));
        Thread stderr = new Thread(() -> drain(child.getErrorStream()), "codex-app-server-stderr");
        stderr.setDaemon(true);
        stderr.start();
        Thread stdout = new Thread(() -> readMessages(child, events, approvalRequests), "codex-app-server-stdout");
        stdout.setDaemon(true);
        stdout.start();
        var clientInfo = mapper.createObjectNode().put("name", "local-agent-orchestrator").put("title", "Local Agent Orchestrator").put("version", "1.0");
        try {
            var initializeParams = mapper.createObjectNode();
            initializeParams.set("clientInfo", clientInfo);
            initializeParams.set("capabilities", mapper.createObjectNode().put("experimentalApi", true));
            send("initialize", initializeParams);
            if (!initialized.await(10, TimeUnit.SECONDS)) throw new IOException("Codex App Server initialization timed out");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt(); stop(); throw new IOException("Initialization interrupted", ex);
        } catch (IOException ex) { stop(); throw ex; }
        if (initializeError != null) { stop(); throw new IOException("Codex App Server initialization failed"); }
        sendNotification("initialized", mapper.createObjectNode());
    }

    public void startThread(String prompt) throws IOException {
        var params = mapper.createObjectNode();
        params.put("cwd", workspace.toString());
        params.put("sandbox", "workspace-write");
        params.put("approvalPolicy", "on-request");
        params.put("approvalsReviewer", "user");
        params.put("multiAgentMode", "explicitRequestOnly");
        params.put("ephemeral", true);
        params.put("developerInstructions", "Read AGENTS.md, docs/base-standards.md, docs/backend-standards.md, applicable skills, and .codex/agents/*.toml. For each task decide whether delegation helps; when it does, explicitly delegate to the relevant registered role. Roles: enzo_seguridad (security), rucau_arquitecto (architecture), jona_dev1 (backend), jesse_dev2 (web UI), nahitan_devops (runtime/deployment), juli_qa (tests/acceptance). With multiAgentMode explicitRequestOnly, request delegation explicitly to the relevant roles when useful. Do not claim a role is working unless a matching agent event reports it.");
        pendingPrompt = prompt;
        // threadId is supplied by the thread/started notification in processIncoming.
        send("thread/start", params);
    }

    private volatile String pendingPrompt;
    private volatile String threadId;

    public void decide(String approvalId, String decision) throws IOException {
        JsonNode rpcId = approvals.get(approvalId);
        if (rpcId == null) throw new IllegalArgumentException("Approval is no longer pending");
        var result = mapper.createObjectNode().put("decision", decision);
        var response = mapper.createObjectNode().put("jsonrpc", "2.0");
        response.set("id", rpcId);
        response.set("result", result);
        write(response);
        approvals.remove(approvalId, rpcId);
    }

    public void interrupt() throws IOException {
        if (threadId != null) send("turn/interrupt", mapper.createObjectNode().put("threadId", threadId));
    }

    @PreDestroy public synchronized void stop() {
        if (process != null) process.destroy();
        process = null;
        threadId = null;
        pendingPrompt = null;
        approvals.clear();
    }

    private void readMessages(Process child, Consumer<JsonNode> events, Consumer<JsonNode> approvalRequests) {
        try (var reader = new BufferedReader(new InputStreamReader(child.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonNode message;
                try { message = mapper.readTree(line); } catch (Exception ignored) { continue; }
                processIncoming(child, message, events, approvalRequests);
            }
        } catch (IOException ignored) { }
        finally {
            if (process == child) events.accept(mapper.createObjectNode().put("method", "app-server/disconnected"));
        }
    }

    private void processIncoming(Process child, JsonNode message, Consumer<JsonNode> events, Consumer<JsonNode> approvalRequests) {
        if (process != child) return;
        if (message.has("id") && message.has("method")) {
            String id = message.path("id").asText();
            String method = message.path("method").asText();
            if (method.toLowerCase().contains("approval") || method.toLowerCase().contains("commandexecution") || method.toLowerCase().contains("filechange")) {
                approvals.put(id, message.get("id").deepCopy());
                approvalRequests.accept(message);
            } else {
                try {
                    var response = mapper.createObjectNode().put("jsonrpc", "2.0");
                    response.set("id", message.get("id"));
                    response.set("error", mapper.createObjectNode().put("code", -32601).put("message", "Unsupported request"));
                    write(response);
                }
                catch (IOException ignored) { }
            }
            return;
        }
        if (message.has("id") && (message.has("result") || message.has("error"))) {
            String requestId = message.path("id").asText();
            String requestMethod = pendingRequests.remove(requestId);
            if (message.has("error")) {
                JsonNode error = message.path("error");
                String code = error.path("code").asText("unknown");
                String errorMessage = safeLogMessage(error.path("message").asText("No error message returned"));
                logger.warn("Codex App Server request '{}' failed (code={}): {}", requestMethod, code, errorMessage);
                if ("initialize".equals(requestMethod)) { initializeError = code + ": " + errorMessage; initialized.countDown(); return; }
                if ("thread/start".equals(requestMethod)) { events.accept(mapper.createObjectNode().put("method", "app-server/thread-start-failed")); return; }
                if ("turn/start".equals(requestMethod)) { events.accept(mapper.createObjectNode().put("method", "app-server/turn-start-failed")); return; }
                return;
            }
            if ("initialize".equals(requestMethod)) { initialized.countDown(); return; }
            if ("thread/start".equals(requestMethod)) {
                JsonNode thread = message.path("result").path("thread");
                if (!message.has("error") && !thread.isMissingNode() && !thread.path("id").asText().isBlank()) {
                    threadId = thread.path("id").asText();
                    var started = mapper.createObjectNode().put("method", "thread/started");
                    started.set("params", mapper.createObjectNode().set("thread", thread));
                    events.accept(started);
                    launchPendingTurn(child, events);
                } else {
                    logger.warn("Codex App Server 'thread/start' response did not include a thread id (requestId={})", requestId);
                    events.accept(mapper.createObjectNode().put("method", "app-server/thread-start-failed"));
                }
                return;
            }
            return;
        }
        String method = message.path("method").asText();
        if ("thread/started".equals(method)) {
            threadId = message.path("params").path("thread").path("id").asText(null);
            launchPendingTurn(child, events);
        }
        events.accept(message);
    }

    private void send(String method, JsonNode params) throws IOException {
        long id = requestIds.incrementAndGet();
        pendingRequests.put(Long.toString(id), method);
        write(mapper.createObjectNode().put("jsonrpc", "2.0").put("id", id).put("method", method).set("params", params));
    }
    private void sendNotification(String method, JsonNode params) throws IOException {
        write(mapper.createObjectNode().put("jsonrpc", "2.0").put("method", method).set("params", params));
    }
    private synchronized void write(JsonNode value) throws IOException {
        if (input == null) throw new IOException("Codex App Server is not connected");
        input.write(mapper.writeValueAsString(value)); input.newLine(); input.flush();
    }
    private void drain(java.io.InputStream stream) {
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            int loggedLines = 0;
            while ((line = reader.readLine()) != null) {
                String lower = line.toLowerCase(java.util.Locale.ROOT);
                if (loggedLines < 20 && (lower.contains("error") || lower.contains("failed") || lower.contains("denied") || lower.contains("warning"))) {
                    logger.warn("Codex App Server stderr: {}", safeLogMessage(line));
                    loggedLines++;
                }
            }
        } catch (IOException ex) {
            logger.debug("Could not read Codex App Server stderr: {}", safeLogMessage(ex.getMessage()));
        }
    }
    private String safeLogMessage(String message) {
        if (message == null || message.isBlank()) return "No error message provided";
        String normalized = message.replaceAll("[\\r\\n\\t]+", " ")
                .replaceAll("(?i)(authorization|api[-_ ]?key|access[-_ ]?token|password)(\\s*[:=]\\s*)\\S+", "$1$2[REDACTED]")
                .replaceAll("(?i)Bearer\\s+\\S+", "Bearer [REDACTED]");
        return normalized.length() > 500 ? normalized.substring(0, 500) : normalized;
    }
    private synchronized void launchPendingTurn(Process child, Consumer<JsonNode> events) {
        if (process != child) return;
        String prompt = pendingPrompt;
        if (threadId == null || prompt == null) return;
        pendingPrompt = null;
        try {
            var inputItems = mapper.createArrayNode().add(mapper.createObjectNode().put("type", "text").put("text", prompt));
            send("turn/start", mapper.createObjectNode().put("threadId", threadId).set("input", inputItems));
        } catch (IOException ignored) { events.accept(mapper.createObjectNode().put("method", "app-server/turn-start-failed")); }
    }

    @FunctionalInterface
    interface ProcessStarter { Process start(ProcessBuilder builder) throws IOException; }
}
