package com.spring.ia.util;

import com.spring.ia.dto.AgentStatus;
import com.spring.ia.dto.AgentTaskStatus;
import java.util.Map;

public record CodexAgentEventUpdate(AgentTaskStatus taskStatus, Map<String, AgentStatus> agentStatuses,
                                   String summary, String response) {}
