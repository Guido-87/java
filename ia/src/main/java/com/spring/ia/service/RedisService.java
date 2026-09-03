package com.spring.ia.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Slf4j
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
            long inicio = System.nanoTime();
            String json = redisTemplate.opsForValue().get("chat:" + userId);
            log.info("Redis GET chat:{} tardó {} ms", userId, medirDuracion(inicio));
            if (json == null) return new ArrayList<>();
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error al obtener conversación para userId={}", userId, e);
            return new ArrayList<>();
        }
    }

    public void guardarConversacion(String userId, List<Map<String, String>> mensajes) {
        try {
            long inicio = System.nanoTime();
            String json = mapper.writeValueAsString(mensajes);
            redisTemplate.opsForValue().set("chat:" + userId, json, TTL);
            log.info("Redis SET chat:{} tardó {} ms", userId, medirDuracion(inicio));
        } catch (Exception e) {
            log.error("Error al guardar conversación para userId={}", userId, e);
        }
    }

    private long medirDuracion(long inicio) {
        return (System.nanoTime() - inicio) / 1_000_000;
    }
}