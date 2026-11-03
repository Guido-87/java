package com.spring.ia.controller;

import com.spring.ia.dto.AgentApprovalDecisionRequest;
import com.spring.ia.dto.AgentTaskRequest;
import com.spring.ia.service.AgentTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AgentTaskControllerTest {
    private final AgentTaskService service = mock(AgentTaskService.class);
    private final AgentTaskController controller = new AgentTaskController(service);

    @Test void delegatesEndpointsAndEnforcesLoopbackOriginOnMutations() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session); request.setLocalPort(8080); request.setServerName("127.0.0.1"); request.setServerPort(8080); request.setScheme("http");
        request.addHeader("Origin", "http://127.0.0.1:8080");

        controller.status(session);
        controller.submit(new AgentTaskRequest("task", "manual"), request);
        controller.get("task-1", session);
        controller.events("task-1", session);
        controller.approval("task-1", "approval-1", new AgentApprovalDecisionRequest("decline"), request);
        controller.interrupt("task-1", request);

        verify(service).status(session.getId());
        verify(service).submit(session.getId(), "task", "manual");
        verify(service).get(session.getId(), "task-1");
        verify(service).events(session.getId(), "task-1");
        verify(service).approve(session.getId(), "task-1", "approval-1", "decline");
        verify(service).interrupt(session.getId(), "task-1");
        assertThat(controller.submit(new AgentTaskRequest("task", "manual"), request).getStatusCode().value()).isEqualTo(202);

        MockHttpServletRequest badRequest = new MockHttpServletRequest();
        badRequest.setSession(session); badRequest.setLocalPort(8080); badRequest.setServerName("127.0.0.1"); badRequest.setServerPort(8080); badRequest.setScheme("http");
        badRequest.addHeader("Origin", "https://example.com:8080");
        assertThatThrownBy(() -> controller.submit(new AgentTaskRequest("task", "manual"), badRequest))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test void mapsExpiredApprovalToConflictAndPreservesCause() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = localRequest(session);
        var cause = new IllegalArgumentException("Approval is no longer pending");
        when(service.approve(session.getId(), "task-1", "approval-1", "accept")).thenThrow(cause);

        assertThatThrownBy(() -> controller.approval("task-1", "approval-1",
                new AgentApprovalDecisionRequest("accept"), request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(exception.getReason()).contains("ya no está pendiente", "estado actual");
                    assertThat(exception.getCause()).isSameAs(cause);
                });
    }

    @Test void mapsMissingApprovalDetailsToBadRequestWithClearMessage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        MockHttpServletRequest request = localRequest(session);
        var cause = new IllegalArgumentException("No están disponibles los detalles de aprobación; rechazá la solicitud");
        when(service.approve(session.getId(), "task-1", "approval-1", "accept")).thenThrow(cause);

        assertThatThrownBy(() -> controller.approval("task-1", "approval-1",
                new AgentApprovalDecisionRequest("accept"), request))
                .isInstanceOfSatisfying(ResponseStatusException.class, exception -> {
                    assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception.getReason()).contains("No están disponibles", "rechazá la solicitud");
                    assertThat(exception.getCause()).isSameAs(cause);
                });
    }

    private MockHttpServletRequest localRequest(MockHttpSession session) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(session);
        request.setLocalPort(8080);
        request.setServerName("127.0.0.1");
        request.setServerPort(8080);
        request.setScheme("http");
        request.addHeader("Origin", "http://127.0.0.1:8080");
        return request;
    }
}
