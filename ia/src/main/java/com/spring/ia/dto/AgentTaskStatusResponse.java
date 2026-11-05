package com.spring.ia.dto;

import java.util.List;

public record AgentTaskStatusResponse(boolean enabled, String connection, String approvalMode, AgentTaskSnapshot activeTask,
                                      List<AgentState> agents) {}
