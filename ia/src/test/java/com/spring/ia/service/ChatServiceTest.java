package com.spring.ia.service;

import com.spring.ia.client.GroqClient;
import com.spring.ia.dto.ChatRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService Tests")
class ChatServiceTest {

    @Mock
    private GroqClient groqClient;

    @Mock
    private RedisService redisService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private ChatService chatService;

    @Test
    @DisplayName("Debería retornar respuesta de IA")
    void shouldReturnResponseFromAI() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta mock");

        ChatRequest request = new ChatRequest("hola");

        String result = chatService.chat(request, session);

        assertEquals("respuesta mock", result);
    }

    @Test
    @DisplayName("Debería crear userId si no existe en sesión")
    void shouldCreateUserIdIfNotExists() {

        when(session.getAttribute("userId")).thenReturn(null);

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("ok");

        ChatRequest request = new ChatRequest("hola");

        String result = chatService.chat(request, session);

        assertNotNull(result);

        verify(session).setAttribute(eq("userId"), anyString());
    }

    @Test
    @DisplayName("Debería guardar conversación en Redis")
    void shouldSaveConversationToRedis() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion("user123"))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta");

        ChatRequest request = new ChatRequest("hola");

        chatService.chat(request, session);

        verify(redisService).guardarConversacion(eq("user123"), anyList());
    }

    @Test
    @DisplayName("Debería llamar GroqClient")
    void shouldCallGroqClient() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta");

        ChatRequest request = new ChatRequest("hola");

        chatService.chat(request, session);

        verify(groqClient, times(1))
                .completeChat(anyList(), anyString());
    }

    @Test
    @DisplayName("No debería retornar null")
    void shouldNotReturnNull() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta");

        ChatRequest request = new ChatRequest("hola");

        String result = chatService.chat(request, session);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Debería resumir la conversación cuando supera 10 mensajes")
    void shouldSummarizeConversationWhenExceedsLimit() {

        when(session.getAttribute("userId")).thenReturn("user123");

        List<Map<String, String>> mensajes = new ArrayList<>();

        for (int i = 0; i < 11; i++) {
            mensajes.add(Map.of(
                    "role", i % 2 == 0 ? "user" : "assistant",
                    "content", "mensaje " + i
            ));
        }

        when(redisService.obtenerConversacion("user123"))
                .thenReturn(mensajes);

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("Resumen de la conversación");

        ChatRequest request = new ChatRequest("nuevo mensaje");

        String result = chatService.chat(request, session);

        assertEquals("Resumen de la conversación", result);

        verify(groqClient, atLeast(2))
                .completeChat(anyList(), anyString());

        verify(redisService)
                .guardarConversacion(eq("user123"), anyList());
    }

    @Test
    @DisplayName("Debería usar fallback si Groq falla")
    void shouldUseFallbackWhenGroqFails() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), eq("openai/gpt-oss-20b")))
                .thenThrow(new RuntimeException("Groq caído"))
                .thenReturn("respuesta fallback");

        ChatRequest request = new ChatRequest("hola");

        String result = chatService.chat(request, session);

        assertEquals("respuesta fallback", result);

        verify(groqClient, times(2))
                .completeChat(anyList(), anyString());
    }

    @Test
    @DisplayName("Debería seleccionar modelo avanzado para consultas complejas")
    void shouldSelectAdvancedModelForComplexPrompt() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), eq("openai/gpt-oss-120b")))
                .thenReturn("respuesta avanzada");

        ChatRequest request = new ChatRequest(
                "explica la arquitectura de microservicios con Spring"
        );

        String result = chatService.chat(request, session);

        assertEquals("respuesta avanzada", result);

        verify(groqClient)
                .completeChat(anyList(), eq("openai/gpt-oss-120b"));
    }

    @Test
    @DisplayName("Debería agregar prompt de experto para consultas de Java")
    void shouldUseExpertSystemPromptForJava() {

        when(session.getAttribute("userId")).thenReturn("user123");

        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta");

        ChatRequest request = new ChatRequest(
                "¿Cómo implementar Redis con Spring Boot?"
        );

        chatService.chat(request, session);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Map<String, String>>> captor =
                ArgumentCaptor.forClass(List.class);

        verify(groqClient).completeChat(captor.capture(), anyString());

        List<Map<String, String>> mensajes = captor.getValue();

        assertEquals("system", mensajes.getFirst().get("role"));
        assertTrue(mensajes.getFirst().get("content").contains("Java"));
        assertTrue(mensajes.getFirst().get("content").contains("Spring Boot"));
    }
}