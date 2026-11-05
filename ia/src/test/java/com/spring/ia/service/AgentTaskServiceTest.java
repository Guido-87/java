package com.spring.ia.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ia.client.CodexAppServerClient;
import com.spring.ia.dto.AgentTaskStatus;
import com.spring.ia.dto.AgentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AgentTaskServiceTest {
    @TempDir Path workspace;
    private final ObjectMapper mapper = new ObjectMapper();
    private CodexAppServerClient client;
    private AgentTaskService service;
    private AtomicReference<Consumer<JsonNode>> events;
    private AtomicReference<Consumer<JsonNode>> approvals;

    @BeforeEach void setUp() throws Exception {
        client = mock(CodexAppServerClient.class);
        when(client.isRunning()).thenReturn(true);
        events = new AtomicReference<>(); approvals = new AtomicReference<>();
        doAnswer(invocation -> { events.set(invocation.getArgument(0)); approvals.set(invocation.getArgument(1)); return null; })
                .when(client).start(any(), any());
        service = new AgentTaskService(true, "manual", workspace.toString(), 1800, 30, client);
    }

    @Test void reportsDisabledOrDisconnectedAndTheRegisteredRoster() {
        var disabled = new AgentTaskService(false, "automatic", workspace.toString(), 1800, 30, client).status("s");
        assertThat(disabled.connection()).isEqualTo("disabled");
        assertThat(disabled.enabled()).isFalse();
        assertThat(disabled.approvalMode()).isEqualTo("automatic");
        when(client.isRunning()).thenReturn(false);
        var status = service.status("s");
        assertThat(status.connection()).isEqualTo("disconnected");
        assertThat(status.agents()).hasSize(6).allMatch(agent -> agent.status() == AgentStatus.AVAILABLE);
    }

    @Test void tracksEventsApprovalsCompletionAndSessionOwnership() throws Exception {
        var snapshot = service.submit("session-a", "do a task", "manual");
        assertThat(snapshot.status()).isEqualTo(AgentTaskStatus.QUEUED);
        assertThat(snapshot.approvalMode()).isEqualTo("manual");
        verify(client).startThread("do a task");
        assertThat(service.status("session-a").activeTask().taskId()).isEqualTo(snapshot.taskId());
        assertThatThrownBy(() -> service.get("session-b", snapshot.taskId())).isInstanceOf(IllegalArgumentException.class);
        assertThat(service.events("session-a", snapshot.taskId())).isNotNull();

        events.get().accept(mapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"root","agentRole":"orchestrator"}}}
                """));
        events.get().accept(mapper.readTree("""
                {"method":"item/started","params":{"threadId":"root","item":{"type":"collabAgentToolCall","agentsStates":{"jona_dev1":{"status":"running"}}}}}
                """));
        assertThat(service.get("session-a", snapshot.taskId()).agents())
                .anySatisfy(agent -> { assertThat(agent.id()).isEqualTo("jona_dev1"); assertThat(agent.status()).isEqualTo(AgentStatus.WORKING); });

        approvals.get().accept(mapper.readTree("""
                {"jsonrpc":"2.0","id":"approval-1","method":"item/commandExecution/requestApproval","params":{"command":["git","status"],"cwd":"C:/project"}}
                """));
        var waiting = service.get("session-a", snapshot.taskId());
        assertThat(waiting.status()).isEqualTo(AgentTaskStatus.WAITING_APPROVAL);
        assertThat(waiting.approval().details()).contains("git", "Directorio de trabajo");
        service.approve("session-a", snapshot.taskId(), "approval-1", "decline");
        verify(client).decide("approval-1", "decline");

        events.get().accept(mapper.readTree("""
                {"method":"item/completed","params":{"threadId":"root","item":{"type":"agentMessage","phase":"final_answer","text":"The requested task is complete."}}}
                """));
        events.get().accept(mapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"root","turn":{"status":"completed"}}}
                """));
        var completed = service.get("session-a", snapshot.taskId());
        assertThat(completed.status()).isEqualTo(AgentTaskStatus.COMPLETED);
        assertThat(completed.response()).isEqualTo("The requested task is complete.");
        assertThat(completed.agents()).anySatisfy(agent -> {
            assertThat(agent.id()).isEqualTo("jona_dev1"); assertThat(agent.status()).isEqualTo(AgentStatus.COMPLETED);
        });
        assertThat(completed.agents()).anySatisfy(agent -> {
            assertThat(agent.id()).isEqualTo("enzo_seguridad"); assertThat(agent.status()).isEqualTo(AgentStatus.AVAILABLE);
        });
    }

    @Test void failedAndInterruptedTasksFinalizeOnlyAgentsStillActive() throws Exception {
        var failed = service.submit("failed-session", "task", "manual");
        events.get().accept(mapper.readTree("{\"method\":\"thread/started\",\"params\":{\"thread\":{\"id\":\"failed-root\"}}}"));
        events.get().accept(mapper.readTree("""
                {"method":"item/started","params":{"threadId":"failed-root","item":{"type":"collabAgentToolCall","agentsStates":{"juli_qa":{"status":"running"},"jona_dev1":{"status":"pendingInit"}}}}}
                """));
        events.get().accept(mapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"failed-root","turn":{"status":"failed"}}}
                """));
        assertThat(service.get("failed-session", failed.taskId()).agents()).allSatisfy(agent -> {
            if (agent.id().equals("juli_qa") || agent.id().equals("jona_dev1")) assertThat(agent.status()).isEqualTo(AgentStatus.FAILED);
        });

        var interrupted = service.submit("interrupted-session", "task", "manual");
        events.get().accept(mapper.readTree("{\"method\":\"thread/started\",\"params\":{\"thread\":{\"id\":\"interrupted-root\"}}}"));
        events.get().accept(mapper.readTree("""
                {"method":"item/started","params":{"threadId":"interrupted-root","item":{"type":"collabAgentToolCall","agentsStates":{"juli_qa":{"status":"running"}}}}}
                """));
        events.get().accept(mapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"interrupted-root","turn":{"status":"interrupted"}}}
                """));
        assertThat(service.get("interrupted-session", interrupted.taskId()).agents()).anySatisfy(agent -> {
            assertThat(agent.id()).isEqualTo("juli_qa"); assertThat(agent.status()).isEqualTo(AgentStatus.INTERRUPTED);
        });
    }

    @Test void automaticallyApprovesFileChangesInsideWorktreeEvenInManualMode() throws Exception {
        var snapshot = service.submit("file-session", "edit project files", "manual");
        events.get().accept(mapper.readTree("{\"method\":\"thread/started\",\"params\":{\"thread\":{\"id\":\"file-root\"}}}"));
        approvals.get().accept(mapper.readTree("""
                {"jsonrpc":"2.0","id":"workspace-delete","method":"item/fileChange/requestApproval","params":{"fileChanges":{"src/Old.java":{"type":"delete","content":"old"}}}}
                """));
        verify(client).decide("workspace-delete", "accept");
        assertThat(service.get("file-session", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WORKING);

        approvals.get().accept(mapper.readTree("""
                {"jsonrpc":"2.0","id":"outside-update","method":"item/fileChange/requestApproval","params":{"fileChanges":{"C:/outside/App.java":{"type":"update","unified_diff":"change"}}}}
                """));
        assertThat(service.get("file-session", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WAITING_APPROVAL);
        service.approve("file-session", snapshot.taskId(), "outside-update", "decline");
    }

    @Test void automaticModeApprovesOnlySchemaClassifiedReadOnlyCommands() throws Exception {
        var snapshot = service.submit("session-a", "inspect", "automatic");
        events.get().accept(mapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"root"}}}
                """));
        var safe = mapper.createObjectNode().put("jsonrpc", "2.0").put("id", "approval-safe").put("method", "execCommandApproval");
        var params = mapper.createObjectNode().put("cwd", workspace.toString());
        params.set("parsedCmd", mapper.createArrayNode().add(mapper.createObjectNode().put("type", "list_files").putNull("path")));
        safe.set("params", params);
        approvals.get().accept(safe);
        verify(client).decide("approval-safe", "accept");
        assertThat(service.get("session-a", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WORKING);

        var fileUpdate = mapper.createObjectNode().put("jsonrpc", "2.0").put("id", "approval-file-update")
                .put("method", "item/fileChange/requestApproval");
        var update = mapper.createObjectNode().put("type", "update").put("unified_diff", "@@ -1 +1 @@\\n-old\\n+new");
        fileUpdate.set("params", mapper.createObjectNode().set("fileChanges", mapper.createObjectNode()
                .set(workspace.resolve("src/App.java").toString(), update)));
        approvals.get().accept(fileUpdate);
        verify(client).decide("approval-file-update", "accept");

        var risky = mapper.createObjectNode().put("jsonrpc", "2.0").put("id", "approval-risk").put("method", "item/commandExecution/requestApproval");
        risky.set("params", mapper.createObjectNode().put("command", "rm -rf something").put("cwd", workspace.toString()));
        approvals.get().accept(risky);
        assertThat(service.get("session-a", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WAITING_APPROVAL);
        assertThatThrownBy(() -> service.approve("session-a", snapshot.taskId(), "wrong-approval-id", "accept"))
                .isInstanceOf(IllegalArgumentException.class);
        service.approve("session-a", snapshot.taskId(), "approval-risk", "decline");

        var deletion = mapper.createObjectNode().put("jsonrpc", "2.0").put("id", "approval-file-delete")
                .put("method", "item/fileChange/requestApproval");
        var delete = mapper.createObjectNode().put("type", "delete").put("content", "old source");
        deletion.set("params", mapper.createObjectNode().set("fileChanges", mapper.createObjectNode()
                .set(workspace.resolve("src/Old.java").toString(), delete)));
        approvals.get().accept(deletion);
        verify(client).decide("approval-file-delete", "accept");
        assertThat(service.get("session-a", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WORKING);
    }

    @Test void exposesAutomaticApprovalWriteFailuresForManualReview() throws Exception {
        var snapshot = service.submit("approval-failure-session", "inspect", "automatic");
        events.get().accept(mapper.readTree("{\"method\":\"thread/started\",\"params\":{\"thread\":{\"id\":\"approval-failure-root\"}}}"));
        var approval = mapper.readTree("""
                {"jsonrpc":"2.0","id":"approval-read-retry","method":"execCommandApproval","params":{"command":["Get-ChildItem"],"cwd":"%s","parsedCmd":[{"type":"list_files","path":null}]}}
                """.formatted(workspace.toString().replace("\\", "\\\\")));
        doThrow(new IOException("write failed")).when(client).decide("approval-read-retry", "accept");

        approvals.get().accept(approval);

        var waiting = service.get("approval-failure-session", snapshot.taskId());
        assertThat(waiting.status()).isEqualTo(AgentTaskStatus.WAITING_APPROVAL);
        assertThat(waiting.summary()).isEqualTo("Falló la aprobación automática; se requiere aprobación manual");
        assertThat(waiting.approval()).isNotNull();
        assertThat(waiting.approval().id()).isEqualTo("approval-read-retry");
        assertThat(waiting.approval().canAccept()).isTrue();

        doNothing().when(client).decide("approval-read-retry", "accept");
        var accepted = service.approve("approval-failure-session", snapshot.taskId(), "approval-read-retry", "accept");
        assertThat(accepted.status()).isEqualTo(AgentTaskStatus.WORKING);
        verify(client, times(2)).decide("approval-read-retry", "accept");
    }

    @Test void handlesLaunchFailureAndInterruptWithoutLeakingPrompt() throws Exception {
        doThrow(new IOException("private process detail")).when(client).startThread("secret prompt");
        var failed = service.submit("s1", "secret prompt", null);
        assertThat(failed.status()).isEqualTo(AgentTaskStatus.FAILED);
        assertThat(failed.summary()).doesNotContain("secret prompt", "private process detail");

        reset(client);
        when(client.isRunning()).thenReturn(true);
        doAnswer(invocation -> { events.set(invocation.getArgument(0)); approvals.set(invocation.getArgument(1)); return null; }).when(client).start(any(), any());
        service = new AgentTaskService(true, "manual", workspace.toString(), 1800, 30, client);
        var queued = service.submit("s2", "another task", null);
        assertThat(service.interrupt("s2", queued.taskId()).status()).isEqualTo(AgentTaskStatus.INTERRUPTED);
        verify(client).interrupt(); verify(client).stop();
    }

    @Test void taskTimeoutStopsTheLocalProcessAndMarksTheTaskFailed() throws Exception {
        service = new AgentTaskService(true, "manual", workspace.toString(), 1, 30, client);
        var queued = service.submit("timeout-session", "long task", null);
        Thread.sleep(1200);
        assertThat(service.get("timeout-session", queued.taskId()).status()).isEqualTo(AgentTaskStatus.FAILED);
        verify(client).stop();
        service.shutdown();
    }

    @Test void failsQueuedTaskWhenCodexDoesNotStartWithinStartupTimeout() throws Exception {
        service = new AgentTaskService(true, "manual", workspace.toString(), 1800, 1, client);
        var queued = service.submit("startup-timeout-session", "task", null);
        Thread.sleep(1200);

        var failed = service.get("startup-timeout-session", queued.taskId());
        assertThat(failed.status()).isEqualTo(AgentTaskStatus.FAILED);
        assertThat(failed.summary()).isEqualTo("Codex superó el tiempo límite para iniciar la tarea");
        verify(client).stop();
        service.shutdown();
    }

    @Test void acceptsRootThreadWhenParentThreadIdIsExplicitlyNull() throws Exception {
        service = new AgentTaskService(true, "manual", workspace.toString(), 1800, 1, client);
        var queued = service.submit("null-parent-session", "task", null);
        events.get().accept(mapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"root-with-null-parent","parentThreadId":null}}}
                """));
        Thread.sleep(1200);

        var started = service.get("null-parent-session", queued.taskId());
        assertThat(started.status()).isEqualTo(AgentTaskStatus.WORKING);
        assertThat(started.summary()).isEqualTo("Tarea en curso");
        verify(client, never()).stop();
        service.shutdown();
    }

    @Test void movesQueuedTaskToWorkingWhenRootThreadStarts() throws Exception {
        var snapshot = service.submit("started-session", "task", "manual");
        events.get().accept(mapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"started-root"}}}
                """));

        assertThat(service.get("started-session", snapshot.taskId()).status()).isEqualTo(AgentTaskStatus.WORKING);
    }

}
