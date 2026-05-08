package com.spring.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ia.exception.GroqClientException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cliente reutilizable para Groq API.
 * Encapsula toda la lógica de comunicación HTTP con Groq.
 */
@Component
public class GroqClient {

    private static final Logger log = LoggerFactory.getLogger(GroqClient.class);
    private static final int TIMEOUT_SECONDS = 10;
    private static final int MAX_RETRIES = 2;

    private final WebClient webClient;
    private final ObjectMapper mapper;

    public GroqClient(WebClient.Builder builder,
                      @Value("${groq.api.base-url:https://api.groq.com/openai}") String baseUrl,
                      @Value("${groq.api.key:}") String apiKey) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.mapper = new ObjectMapper();
    }

    /**
     * Completa un chat usando Groq.
     *
     * @param mensajes Lista de mensajes en el formato OpenAI (role, content)
     * @param model Modelo a usar (ej: "llama-3.1-70b-versatile")
     * @return Respuesta del modelo
     * @throws GroqClientException Si hay error en la llamada
     */
    public String completeChat(List<Map<String, String>> mensajes, String model) {
        log.info("Llamando Groq con modelo: {}", model);

        String response = callGroqApi(mensajes, model);
        return extractResponse(response);
    }

    /**
     * Realiza la llamada HTTP a Groq.
     */
    private String callGroqApi(List<Map<String, String>> mensajes, String model) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("messages", mensajes);
            log.info("Llamando a Groq con modelo: {}", model);
            String response = webClient.post()
                    .uri("/v1/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .retry(MAX_RETRIES)
                    .block();
            log.info("Response final: {}", response);
            if (response == null) {
                throw new GroqClientException("Respuesta vacía de Groq");
            }
            return response;
        } catch (Exception e) {
            log.error("Error llamando a Groq API", e);
            throw new GroqClientException("Error comunicándose con Groq: " + e.getMessage(), e);
        }
    }

    /**
     * Extrae el contenido de la respuesta de Groq.
     */
    private String extractResponse(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            log.error("Error parseando respuesta de Groq", e);
            throw new GroqClientException("Error parseando respuesta: " + e.getMessage(), e);
        }
    }
}
