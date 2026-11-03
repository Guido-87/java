package com.spring.ia.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentApprovalDecisionRequest(@NotBlank(message = "La decisión de aprobación no puede estar vacía") String decision) {
    public AgentApprovalDecisionRequest {
        if (!"accept".equals(decision) && !"decline".equals(decision)) throw new IllegalArgumentException("decision must be accept or decline");
    }
}
