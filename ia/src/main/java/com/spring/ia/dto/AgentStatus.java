package com.spring.ia.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AgentStatus {
    QUEUED("queued"), WORKING("working"), WAITING_APPROVAL("waiting_approval"), COMPLETED("completed"), FAILED("failed"), INTERRUPTED("interrupted"), AVAILABLE("available"), DISCONNECTED("disconnected");
    private final String value;
    AgentStatus(String value) { this.value = value; }
    @JsonValue public String value() { return value; }
    public static AgentStatus fromCodex(String value) {
        if (value == null) return WORKING;
        return switch (value) {
            case "pendingInit" -> QUEUED;
            case "running" -> WORKING;
            case "completed" -> COMPLETED;
            case "failed" -> FAILED;
            case "interrupted" -> INTERRUPTED;
            case "shutdown", "notFound" -> DISCONNECTED;
            default -> WORKING;
        };
    }
}
