package com.spring.ia.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.spring.ia.dto.AgentStatus;
import com.spring.ia.dto.AgentTaskStatus;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CodexAgentEventMapper {
    public static final Set<String> AGENT_IDS = Set.of("enzo_seguridad", "rucau_arquitecto", "jona_dev1", "jesse_dev2", "nahitan_devops", "juli_qa");

    public CodexAgentEventUpdate map(JsonNode event, String rootThreadId) {
        String method = event.path("method").asText();
        JsonNode params = event.path("params");
        String threadId = params.path("threadId").asText();
        if ("thread/started".equals(method)) {
            JsonNode thread = params.path("thread");
            if (!rootThreadId.equals(thread.path("parentThreadId").asText())) return empty();
            String role = thread.path("agentRole").asText();
            return new CodexAgentEventUpdate(null, AGENT_IDS.contains(role) ? Map.of(role, AgentStatus.WORKING) : Map.of(), null, null);
        }
        if (!rootThreadId.equals(threadId)) return empty();
        if ("turn/started".equals(method)) {
            return new CodexAgentEventUpdate(AgentTaskStatus.WORKING, Map.of(), null, null);
        }
        Map<String, AgentStatus> agents = new LinkedHashMap<>();
        JsonNode item = params.path("item");
        if ("item/completed".equals(method) && "agentMessage".equals(item.path("type").asText())
                && (!item.has("phase") || item.path("phase").isNull() || "final_answer".equals(item.path("phase").asText()))) {
            return new CodexAgentEventUpdate(null, Map.of(), null, item.path("text").asText(null));
        }
        if (("item/started".equals(method) || "item/completed".equals(method)) && "collabAgentToolCall".equals(item.path("type").asText())) {
            item.path("agentsStates").fields().forEachRemaining(entry -> {
                if (AGENT_IDS.contains(entry.getKey())) agents.put(entry.getKey(), AgentStatus.fromCodex(entry.getValue().path("status").asText()));
            });
        }
        if ("turn/completed".equals(method)) {
            return new CodexAgentEventUpdate(AgentTaskStatus.fromCodex(params.path("turn").path("status").asText()),
                    agents, null, finalAnswerFromTurn(params.path("turn").path("items")));
        }
        String safeSummary = agents.isEmpty() ? null : "Agent activity updated";
        return new CodexAgentEventUpdate(null, agents, safeSummary, null);
    }

    private String finalAnswerFromTurn(JsonNode items) {
        String response = null;
        if (items.isArray()) {
            for (JsonNode candidate : items) {
                if (!"agentMessage".equals(candidate.path("type").asText())) continue;
                JsonNode phase = candidate.get("phase");
                if (phase != null && !phase.isNull() && !"final_answer".equals(phase.asText())) continue;
                String text = candidate.path("text").asText(null);
                if (text != null && !text.isBlank()) response = text;
            }
        }
        return response;
    }

    private CodexAgentEventUpdate empty() { return new CodexAgentEventUpdate(null, Map.of(), null, null); }
}
