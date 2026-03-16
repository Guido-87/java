package com.spring.ia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class ChatService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public String consulta(String mensaje) {

        String url = "https://api.groq.com/openai/v1/chat/completions";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3-70b-8192");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", mensaje);

        messages.add(user);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(url, request, Map.class);

        List choices = (List) response.get("choices");
        Map first = (Map) choices.get(0);
        Map message = (Map) first.get("message");

        return (String) message.get("content");
    }
}
