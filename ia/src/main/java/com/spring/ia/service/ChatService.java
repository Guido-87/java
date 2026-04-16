package com.spring.ia.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class ChatService {

    private static final int MAX_MENSAJES = 10;
    private final ObjectMapper mapper = new ObjectMapper();
    private final WebClient webClient;
    private final RedisService redisService;

    public ChatService(WebClient.Builder builder, @Value("${groq.api.base-url:https://api.groq.com/openai}") String baseUrl,
                       @Value("${groq.api.key:}") String apiKey,
                       RedisService redisService) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.redisService = redisService;
    }

    public String chat(String userId, String prompt) {
        // 1. Traer conversación desde Redis
        List<Map<String, String>> mensajes = redisService.obtenerConversacion(userId);

        // 2. Agregar mensaje del usuario
        mensajes.add(Map.of("role", "user", "content", prompt));

        // 3. Preparar mensajes (resumen + límite)
        mensajes = prepararMensajes(mensajes);

        // 4. Llamar IA
        String respuesta = llamarIA(mensajes, prompt);

        // 5. Guardar respuesta
        mensajes.add(Map.of("role", "assistant", "content", respuesta));

        // 6. Guardar en Redis
        redisService.guardarConversacion(userId, mensajes);
        return respuesta;
    }

    private String llamarIA(List<Map<String, String>> mensajes, String prompt) {
        String model = elegirModelo(prompt);
        String response = webClient.post()
                .uri("/v1/chat/completions")
                .bodyValue(Map.of(
                        "model", model,
                        "messages", mensajes
                ))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .retry(2)
                .block();
        try {
            JsonNode root = mapper.readTree(response);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Error llamando a IA", e);
        }
    }

    private String elegirModelo(String prompt) {
        prompt = prompt.toLowerCase();
        if (prompt.length() > 500) return "llama-3.3-70b-versatile";
        if (prompt.contains("explica") ||
            prompt.contains("analiza") ||
            prompt.contains("por que") ||
            prompt.contains("arquitectura") ||
            prompt.contains("optimizar")) {
            return "llama-3.3-70b-versatile";
        }

        return "llama-3.1-8b-instant";
    }

    private List<Map<String, String>> prepararMensajes(List<Map<String, String>> mensajes) {
        if (mensajes.size() <= MAX_MENSAJES) {
            return mensajes;
        }

        // 1. Generar resumen con IA
        String resumen = generarResumenIA(mensajes);

        // 2. Tomar últimos N mensajes
        List<Map<String, String>> ultimos = mensajes.subList(
            mensajes.size() - MAX_MENSAJES,
            mensajes.size()
        );
        List<Map<String, String>> nuevos = new ArrayList<>(ultimos);

        // 3. Insertar resumen como system
        Map<String, String> resumenMsg = Map.of(
            "role", "system",
            "content", resumen
        );
        nuevos.add(0, resumenMsg);
        return nuevos;
    }

    private String generarResumenIA(List<Map<String, String>> mensajes) {
        String textoPlano = mensajes.stream()
            .map(m -> m.get("role") + ": " + m.get("content"))
            .reduce("", (a, b) -> a + "\n" + b);
        String prompt = "Resumí esta conversación en pocas líneas:\n" + textoPlano;
        return llamarIA(
            List.of(Map.of("role", "user", "content", prompt)),
            prompt
        );
    }
}