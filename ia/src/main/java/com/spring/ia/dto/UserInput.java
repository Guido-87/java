package com.spring.ia.dto;

import jakarta.validation.constraints.NotBlank;

public record UserInput(
    @NotBlank(message = "userId no puede ser vacío")
    String userId,
    @NotBlank(message = "prompt no puede ser vacío")
    String prompt
) {}