package com.spring.ia.controller;

import com.spring.ia.dto.AgentApprovalDecisionRequest;
import com.spring.ia.dto.AgentTaskRequest;
import com.spring.ia.service.AgentTaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/api/agent-tasks")
public class AgentTaskController {
    private final AgentTaskService tasks;
    public AgentTaskController(AgentTaskService tasks) { this.tasks = tasks; }

    @GetMapping("/status") public Object status(HttpSession session) { return tasks.status(session.getId()); }
    @PostMapping public ResponseEntity<?> submit(@Valid @RequestBody AgentTaskRequest body, HttpServletRequest request) {
        checkOrigin(request);
        try { return ResponseEntity.status(HttpStatus.ACCEPTED).body(tasks.submit(request.getSession().getId(), body.prompt().trim(), body.approvalMode())); }
        catch (IllegalStateException ex) { throw new ResponseStatusException(HttpStatus.CONFLICT, "No se pudo iniciar la tarea del agente"); }
    }
    @GetMapping("/{taskId}") public Object get(@PathVariable String taskId, HttpSession session) { return tasks.get(session.getId(), taskId); }
    @GetMapping(value="/{taskId}/events", produces=MediaType.TEXT_EVENT_STREAM_VALUE) public SseEmitter events(@PathVariable String taskId, HttpSession session) { return tasks.events(session.getId(), taskId); }
    @PostMapping("/{taskId}/approvals/{approvalId}") public Object approval(@PathVariable String taskId, @PathVariable String approvalId,
            @Valid @RequestBody AgentApprovalDecisionRequest body, HttpServletRequest request) throws IOException {
        checkOrigin(request);
        try { return tasks.approve(request.getSession().getId(), taskId, approvalId, body.decision()); }
        catch (IllegalArgumentException ex) {
            String internalMessage = ex.getMessage();
            if (internalMessage != null && (internalMessage.contains("Approval is no longer pending")
                    || internalMessage.contains("No longer pending"))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La solicitud de aprobación ya no está pendiente. Revisá el estado actual de la tarea.", ex);
            }
            if (internalMessage != null && internalMessage.contains("No se encontró la tarea")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "La tarea ya no está disponible para esta sesión.", ex);
            }
            if (internalMessage != null && internalMessage.contains("No están disponibles los detalles de aprobación")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No están disponibles los detalles de aprobación; rechazá la solicitud.", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo aplicar la aprobación.", ex);
        }
    }
    @PostMapping("/{taskId}/interrupt") public Object interrupt(@PathVariable String taskId, HttpServletRequest request) throws IOException {
        checkOrigin(request);
        try { return tasks.interrupt(request.getSession().getId(), taskId); }
        catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la tarea"); }
    }
    private void checkOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Se requiere el origen de la solicitud");
        try {
            URI uri = URI.create(origin); String host = uri.getHost();
            String serverName = request.getServerName();
            int originPort = uri.getPort() == -1 ? ("https".equals(uri.getScheme()) ? 443 : 80) : uri.getPort();
            int serverPort = request.getServerPort();
            if (host == null || !Set.of("localhost", "127.0.0.1", "::1", "[::1]").contains(host.toLowerCase(Locale.ROOT))
                    || !host.equalsIgnoreCase(serverName) || !("http".equals(uri.getScheme()) || "https".equals(uri.getScheme()))
                    || !uri.getScheme().equalsIgnoreCase(request.getScheme()) || originPort != serverPort) throw new IllegalArgumentException();
        } catch (IllegalArgumentException ex) { throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El origen debe ser una dirección local"); }
    }
}
