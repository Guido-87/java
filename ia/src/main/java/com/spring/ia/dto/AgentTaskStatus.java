package com.spring.ia.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentTaskStatus {
    QUEUED("queued"), WORKING("working"), WAITING_APPROVAL("waiting_approval"), COMPLETED("completed"), FAILED("failed"), INTERRUPTED("interrupted"), DISCONNECTED("disconnected");
    private final String value;
    AgentTaskStatus(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
    public static AgentTaskStatus fromCodex(String value) {
        if (value == null) return WORKING;
        return switch (value) {
            case "completed" -> COMPLETED;
            case "failed" -> FAILED;
            case "interrupted" -> INTERRUPTED;
            case "waiting_approval" -> WAITING_APPROVAL;
            default -> WORKING;
        };
    }
}
