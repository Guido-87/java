package com.spring.ia.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class RedisService {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Duration TTL = Duration.ofSeconds(3600);

    public RedisService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public List<Map<String, String>> obtenerConversacion(String userId) {
        try {
            String json = redisTemplate.opsForValue().get("chat:" + userId);
            if (json == null) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void guardarConversacion(String userId, List<Map<String, String>> mensajes) {
        try {
            String json = mapper.writeValueAsString(mensajes);
            redisTemplate.opsForValue().set("chat:" + userId, json, TTL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}