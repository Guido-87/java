package com.spring.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record AgentTaskRequest(
        @NotBlank(message = "La tarea no puede estar vacía")
        @Size(max = 8000, message = "La tarea no puede superar los 8000 caracteres") String prompt,
        @Pattern(regexp = "manual|automatic", message = "El modo de aprobación debe ser manual o automático") String approvalMode) {}
