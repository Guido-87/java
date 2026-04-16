package test.java.com.spring.ia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.spring.ia.service.RedisService;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisService = new RedisService(redisTemplate);
    }

    @Test
    void testObtenerConversacion() {
        String jsonData = "[{\"role\":\"user\",\"content\":\"Hello\"}]";
        when(valueOperations.get("chat:user123")).thenReturn(jsonData);

        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testObtenerConversacionNotFound() {
        when(valueOperations.get("chat:nonexistent")).thenReturn(null);

        List<Map<String, String>> result = redisService.obtenerConversacion("nonexistent");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testObtenerConversacionWithInvalidJson() {
        when(valueOperations.get("chat:user123")).thenReturn("invalid json");

        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGuardarConversacion() {
        redisService.guardarConversacion("user123", List.of(Map.of("role", "user", "content", "Hello")));

        verify(valueOperations, times(1)).set(
                eq("chat:user123"),
                anyString(),
                eq(Duration.ofSeconds(3600))
        );
    }
}
