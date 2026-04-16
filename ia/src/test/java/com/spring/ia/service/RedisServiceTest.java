package test.java.com.spring.ia.service;

import com.spring.ia.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisService Tests")
class RedisServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Captor
    private ArgumentCaptor<Duration> durationCaptor;

    private RedisService redisService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        redisService = new RedisService(redisTemplate);
    }

    @Test
    @DisplayName("Debería obtener conversación desde Redis")
    void testObtenerConversacion() {
        // Arrange
        String jsonData = "[{\"role\":\"user\",\"content\":\"Hello\"}]";
        when(valueOperations.get("chat:user123")).thenReturn(jsonData);

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user", result.get(0).get("role"));
        assertEquals("Hello", result.get(0).get("content"));
    }

    @Test
    @DisplayName("Debería retornar lista vacía si conversación no existe")
    void testObtenerConversacionNotFound() {
        // Arrange
        when(valueOperations.get("chat:nonexistent")).thenReturn(null);

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería manejar JSON inválido gracefully")
    void testObtenerConversacionWithInvalidJson() {
        // Arrange
        when(valueOperations.get("chat:user123")).thenReturn("invalid json");

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Debería guardar conversación en Redis con TTL")
    void testGuardarConversacion() {
        // Arrange
        String userId = "user123";
        List<Map<String, String>> conversation = List.of(
                Map.of("role", "user", "content", "Hello"),
                Map.of("role", "assistant", "content", "Hi there!")
        );

        // Act
        redisService.guardarConversacion(userId, conversation);

        // Assert
        verify(valueOperations, times(1)).set(
                eq("chat:" + userId),
                argThat(json -> json.contains("Hello") && json.contains("Hi there!")),
                any(Duration.class)
        );
    }

    @Test
    @DisplayName("Debería usar TTL de 3600 segundos")
    void testGuardarConversacionWithCorrectTTL() {
        // Arrange
        String userId = "user123";
        List<Map<String, String>> conversation = new ArrayList<>();

        // Act
        redisService.guardarConversacion(userId, conversation);

        // Assert
        verify(valueOperations).set(
                anyString(),
                anyString(),
                eq(Duration.ofSeconds(3600))
        );
    }

    @Test
    @DisplayName("Debería usar clave con prefijo 'chat:'")
    void testUseCorrectKeyWithPrefix() {
        // Arrange
        String userId = "user789";
        when(valueOperations.get("chat:" + userId)).thenReturn(null);

        // Act
        redisService.obtenerConversacion(userId);

        // Assert
        verify(valueOperations).get("chat:" + userId);
    }

    @Test
    @DisplayName("Debería obtener conversación con múltiples turnos")
    void testObtenerConversacionWithMultipleTurns() {
        // Arrange
        String complexJson = "["
                + "{\"role\":\"user\",\"content\":\"Primera pregunta\"},"
                + "{\"role\":\"assistant\",\"content\":\"Primera respuesta\"},"
                + "{\"role\":\"user\",\"content\":\"Segunda pregunta\"},"
                + "{\"role\":\"assistant\",\"content\":\"Segunda respuesta\"}"
                + "]";
        when(valueOperations.get("chat:user123")).thenReturn(complexJson);

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        // Assert
        assertEquals(4, result.size());
        assertEquals("Primera pregunta", result.get(0).get("content"));
        assertEquals("Segunda respuesta", result.get(3).get("content"));
    }

    @Test
    @DisplayName("Debería preservar contenido con caracteres especiales")
    void testPreserveSpecialCharacters() {
        // Arrange
        String jsonWithSpecialChars = "[{\"role\":\"user\",\"content\":\"¿Cómo estás? 😊 var x = 1;\"}]";
        when(valueOperations.get("chat:user123")).thenReturn(jsonWithSpecialChars);

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        // Assert
        assertTrue(result.get(0).get("content").contains("😊"));
        assertTrue(result.get(0).get("content").contains("var x = 1;"));
    }

    @Test
    @DisplayName("Debería manejar múltiples usuarios sin colisiones")
    void testMultipleUsersWithoutCollisions() {
        // Arrange
        String user1Data = "[{\"role\":\"user\",\"content\":\"User1\"}]";
        String user2Data = "[{\"role\":\"user\",\"content\":\"User2\"}]";
        
        when(valueOperations.get("chat:user1")).thenReturn(user1Data);
        when(valueOperations.get("chat:user2")).thenReturn(user2Data);

        // Act
        List<Map<String, String>> result1 = redisService.obtenerConversacion("user1");
        List<Map<String, String>> result2 = redisService.obtenerConversacion("user2");

        // Assert
        assertEquals("User1", result1.get(0).get("content"));
        assertEquals("User2", result2.get(0).get("content"));
    }

    @Test
    @DisplayName("Debería guardar conversación vacía correctamente")
    void testGuardarConversacionVacia() {
        // Arrange
        String userId = "user123";
        List<Map<String, String>> emptyConversation = new ArrayList<>();

        // Act
        redisService.guardarConversacion(userId, emptyConversation);

        // Assert
        verify(valueOperations).set(
                eq("chat:" + userId),
                eq("[]"),
                any(Duration.class)
        );
    }

    @Test
    @DisplayName("Debería obtener conversación con contenido largo")
    void testObtenerConversacionWithLongContent() {
        // Arrange
        String longContent = "a".repeat(1000);
        String jsonWithLongContent = "[{\"role\":\"user\",\"content\":\"" + longContent + "\"}]";
        when(valueOperations.get("chat:user123")).thenReturn(jsonWithLongContent);

        // Act
        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        // Assert
        assertEquals(longContent, result.get(0).get("content"));
    }
}

