package com.spring.ia.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ia.dto.AgentTaskStatus;
import com.spring.ia.dto.AgentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodexAgentEventMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CodexAgentEventMapper mapper = new CodexAgentEventMapper();

    @Test
    void mapsOnlyKnownAgentRolesFromStartedSubagentThreads() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"child-1","parentThreadId":"root-1","agentRole":"jona_dev1"}}}
                """);

        var updates = mapper.map(event, "root-1");

        assertThat(updates.taskStatus()).isNull();
        assertThat(updates.agentStatuses()).containsEntry("jona_dev1", AgentStatus.WORKING);
    }

    @Test
    void doesNotAssignUnknownRolesToOneOfTheVisibleAgents() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"thread/started","params":{"thread":{"id":"child-1","parentThreadId":"root-1","agentRole":"unknown_role"}}}
                """);

        var updates = mapper.map(event, "root-1");

        assertThat(updates.agentStatuses()).isEmpty();
    }

    @Test
    void mapsCollaborationStatusesWithoutLeakingPromptText() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"item/started","params":{"threadId":"root-1","item":{"type":"collabAgentToolCall","tool":"spawnAgent","prompt":"sensitive full task text","agentsStates":{"juli_qa":{"status":"running"},"unknown":{"status":"running"}}}}}
                """);

        var updates = mapper.map(event, "root-1");

        assertThat(updates.agentStatuses()).containsEntry("juli_qa", AgentStatus.WORKING);
        assertThat(updates.agentStatuses()).doesNotContainKey("unknown");
        assertThat(updates.summary()).doesNotContain("sensitive full task text");
    }

    @Test
    void mapsTurnCompletionAndInterruption() throws Exception {
        var completed = objectMapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"root-1","turn":{"status":"completed"}}}
                """);
        var interrupted = objectMapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"root-1","turn":{"status":"interrupted"}}}
                """);

        assertThat(mapper.map(completed, "root-1").taskStatus()).isEqualTo(AgentTaskStatus.COMPLETED);
        assertThat(mapper.map(interrupted, "root-1").taskStatus()).isEqualTo(AgentTaskStatus.INTERRUPTED);
    }

    @Test
    void ignoresEventsForOtherThreads() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"other","turn":{"status":"completed"}}}
                """);

        var updates = mapper.map(event, "root-1");

        assertThat(updates.taskStatus()).isNull();
        assertThat(updates.agentStatuses()).isEmpty();
    }

    @Test
    void capturesOnlyFinalAssistantMessageFromTheRootThread() throws Exception {
        var finalAnswer = objectMapper.readTree("""
                {"method":"item/completed","params":{"threadId":"root-1","item":{"type":"agentMessage","phase":"final_answer","text":"Completed the requested work."}}}
                """);
        var commentary = objectMapper.readTree("""
                {"method":"item/completed","params":{"threadId":"root-1","item":{"type":"agentMessage","phase":"commentary","text":"Internal progress."}}}
                """);
        var childAnswer = objectMapper.readTree("""
                {"method":"item/completed","params":{"threadId":"child-1","item":{"type":"agentMessage","phase":"final_answer","text":"Subagent result."}}}
                """);

        assertThat(mapper.map(finalAnswer, "root-1").response()).isEqualTo("Completed the requested work.");
        assertThat(mapper.map(commentary, "root-1").response()).isNull();
        assertThat(mapper.map(childAnswer, "root-1").response()).isNull();
    }
    @Test
    void mapsTurnStartAsWorking() throws Exception {
        var started = objectMapper.readTree("""
                {"method":"turn/started","params":{"threadId":"root-1","turn":{"id":"turn-1"}}}
                """);

        assertThat(mapper.map(started, "root-1").taskStatus()).isEqualTo(AgentTaskStatus.WORKING);
    }

    @Test
    void capturesFinalAssistantMessageWhenPhaseIsOmitted() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"item/completed","params":{"threadId":"root-1","item":{"type":"agentMessage","text":"Final answer without phase."}}}
                """);

        assertThat(mapper.map(event, "root-1").response()).isEqualTo("Final answer without phase.");
    }

    @Test
    void recoversFinalAnswerFromCompletedTurnItems() throws Exception {
        var event = objectMapper.readTree("""
                {"method":"turn/completed","params":{"threadId":"root-1","turn":{"status":"completed","items":[{"type":"agentMessage","text":"Answer from turn snapshot."}]}}}
                """);

        assertThat(mapper.map(event, "root-1").response()).isEqualTo("Answer from turn snapshot.");
    }

}
