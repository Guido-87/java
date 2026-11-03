package com.spring.ia.dto;

import java.time.Instant;
import java.util.List;

public record AgentTaskSnapshot(String taskId, AgentTaskStatus status, String approvalMode, String summary, String response, Instant updatedAt,
                                List<AgentState> agents, AgentApproval approval) {}
