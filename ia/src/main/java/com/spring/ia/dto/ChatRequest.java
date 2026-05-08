package com.spring.ia.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(

    @NotBlank(message = "prompt no puede ser vacío")
    String prompt

) {}