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
}