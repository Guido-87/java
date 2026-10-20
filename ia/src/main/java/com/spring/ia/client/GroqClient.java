package com.spring.ia.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.ia.exception.GroqClientException;

import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Cliente reutilizable para Groq API.
 * Encapsula toda la lógica de comunicación HTTP con Groq.
 */
@Slf4j
@Component
public class GroqClient {

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
        log.info("Llamando Groq con modelo={} requestId={}", model, requestId());
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
            AtomicInteger attempts = new AtomicInteger();
            String response = Mono.defer(() -> {
                        int attempt = attempts.incrementAndGet();
                        long startedAt = System.nanoTime();
                        log.info("Groq attempt started requestId={} attempt={} model={}", requestId(), attempt, model);
                        return webClient.post()
                                .uri("/v1/chat/completions")
                                .bodyValue(body)
                                .retrieve()
                                .onStatus(HttpStatusCode::isError,
                                        clientResponse -> Mono.error(new GroqClientException(
                                                "Groq respondió con estado HTTP " + clientResponse.statusCode())))
                                .bodyToMono(String.class)
                                .doOnSuccess(result -> log.info(
                                        "Groq attempt completed requestId={} attempt={} model={} outcome=success durationMs={}",
                                        requestId(), attempt, model, elapsedMs(startedAt)))
                                .doOnError(error -> log.warn(
                                        "Groq attempt completed requestId={} attempt={} model={} outcome=error errorType={} durationMs={}",
                                        requestId(), attempt, model, error.getClass().getSimpleName(), elapsedMs(startedAt)));
                    })
                    .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
                    .retry(MAX_RETRIES)
                    .block();
            if (response == null) {
                throw new GroqClientException("Respuesta vacía de Groq");
            }
            return response;
        } catch (Exception e) {
            log.error("Error llamando a Groq API requestId={} model={} errorType={}",
                    requestId(), model, e.getClass().getSimpleName());
            throw new GroqClientException("Error comunicándose con Groq (" + e.getClass().getSimpleName() + ")", e);
        }
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private String requestId() {
        String requestId = MDC.get("requestId");
        return requestId == null ? "-" : requestId;
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
