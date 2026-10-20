package com.spring.ia.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
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
        long inicio = System.nanoTime();
        String json;
        try {
            json = redisTemplate.opsForValue().get("chat:" + userId);
            log.info("Redis GET requestId={} outcome=success durationMs={}", requestId(), medirDuracion(inicio));
        } catch (Exception e) {
            Throwable rootCause = rootCause(e);
            log.error("Redis GET requestId={} outcome=error errorType={} durationMs={}",
                    requestId(), rootCause.getClass().getSimpleName(), medirDuracion(inicio));
            return new ArrayList<>();
        }

        if (json == null) return new ArrayList<>();
        try {
            return mapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            Throwable rootCause = rootCause(e);
            log.error("Error al deserializar conversación requestId={} errorType={}",
                    requestId(), rootCause.getClass().getSimpleName());
            return new ArrayList<>();
        }
    }

    public void guardarConversacion(String userId, List<Map<String, String>> mensajes) {
        long inicio = System.nanoTime();
        try {
            String json = mapper.writeValueAsString(mensajes);
            redisTemplate.opsForValue().set("chat:" + userId, json, TTL);
            log.info("Redis SET requestId={} outcome=success durationMs={}", requestId(), medirDuracion(inicio));
        } catch (Exception e) {
            Throwable rootCause = rootCause(e);
            log.error("Redis SET requestId={} outcome=error errorType={} durationMs={}",
                    requestId(), rootCause.getClass().getSimpleName(), medirDuracion(inicio));
        }
    }

    private long medirDuracion(long inicio) {
        return (System.nanoTime() - inicio) / 1_000_000;
    }

    private Throwable rootCause(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private String requestId() {
        String requestId = MDC.get("requestId");
        return requestId == null ? "-" : requestId;
    }
}
