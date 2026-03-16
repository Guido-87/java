package com.spring.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class ChatService {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://api.groq.com/openai/v1")
            .build();

    public String consulta(String mensaje) {
        String apiKey = System.getenv("API_KEY");
        System.out.println(System.getenv("API_KEY"));
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("API_KEY no definida en variables de entorno");
        }
        
        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3-70b-8192");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "user",
                "content", mensaje
        ));

        body.put("messages", messages);

        Map response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        // evitar NullPointerException
        if (response == null) {
            throw new RuntimeException("Respuesta vacía de Groq");
        }

        List choices = (List) response.get("choices");

        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("Groq no devolvió respuestas");
        }

        Map first = (Map) choices.get(0);
        Map message = (Map) first.get("message");

        if (message == null) {
            throw new RuntimeException("Respuesta inválida de Groq");
        }

        return (String) message.get("content");
    }
}
