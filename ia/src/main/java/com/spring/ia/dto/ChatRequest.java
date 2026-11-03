package com.spring.ia.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(

    @NotBlank(message = "El mensaje no puede estar vacío")
    @Size(max = 8000, message = "El mensaje no puede superar los 8000 caracteres")
    String prompt

) {}
