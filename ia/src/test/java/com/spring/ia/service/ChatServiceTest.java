package com.spring.ia.service;

import com.spring.ia.client.GroqClient;
import com.spring.ia.service.ChatService;
import com.spring.ia.service.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Captor
    private ArgumentCaptor<List<Map<String, String>>> messagesCaptor;

    @Captor
    private ArgumentCaptor<String> modelCaptor;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(groqClient, redisService);
    }

    @Test
    @DisplayName("Debería inicializar correctamente")
    void testChatServiceInitialization() {
        assertNotNull(chatService);
    }

    @Test
    @DisplayName("Debería agregar mensaje del usuario a la conversación")
    void testAddUserMessageToConversation() {
        // Arrange
        String userId = "user123";
        String prompt = "¿Hola?";
        List<Map<String, String>> existingConversation = new ArrayList<>();

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(existingConversation);
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Hola, ¿cómo estás?");

        // Act
        chatService.chat(userId, prompt);

        // Assert
        verify(redisService).obtenerConversacion(eq(userId));
        verify(groqClient).completeChat(anyList(), anyString());
        verify(redisService).guardarConversacion(eq(userId), messagesCaptor.capture());
        
        List<Map<String, String>> savedMessages = messagesCaptor.getValue();
        assertTrue(savedMessages.stream().anyMatch(m -> 
            "user".equals(m.get("role")) && prompt.equals(m.get("content"))
        ));
    }

    @Test
    @DisplayName("Debería guardar respuesta del asistente en Redis")
    void testSaveAssistantResponse() {
        // Arrange
        String userId = "user123";
        String userPrompt = "¿Qué es Java?";
        String assistantResponse = "Java es un lenguaje de programación";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn(assistantResponse);

        // Act
        chatService.chat(userId, userPrompt);

        // Assert
        verify(redisService).guardarConversacion(eq(userId), messagesCaptor.capture());
        List<Map<String, String>> savedMessages = messagesCaptor.getValue();
        assertTrue(savedMessages.stream().anyMatch(m ->
                "assistant".equals(m.get("role")) && assistantResponse.equals(m.get("content"))
        ));
    }

    @Test
    @DisplayName("Debería traer conversación previa de Redis")
    void testRetrievePreviousConversation() {
        // Arrange
        String userId = "user123";
        List<Map<String, String>> previousConversation = List.of(
                Map.of("role", "user", "content", "Primer mensaje"),
                Map.of("role", "assistant", "content", "Primera respuesta")
        );

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>(previousConversation));
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Nueva respuesta");

        // Act
        chatService.chat(userId, "Nuevo mensaje");

        // Assert
        verify(redisService).obtenerConversacion(eq(userId));
        verify(groqClient).completeChat(messagesCaptor.capture(), anyString());
        List<Map<String, String>> sentMessages = messagesCaptor.getValue();
        
        assertTrue(sentMessages.stream().anyMatch(m -> 
            "Primer mensaje".equals(m.get("content"))
        ));
    }

    @Test
    @DisplayName("Debería seleccionar modelo pequeño para prompts cortos")
    void testSelectSmallModelForShortPrompt() {
        // Arrange
        String userId = "user123";
        String shortPrompt = "Hola";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Respuesta");

        // Act
        chatService.chat(userId, shortPrompt);

        // Assert
        verify(groqClient).completeChat(anyList(), eq("llama-3.1-8b-instant"));
    }

    @Test
    @DisplayName("Debería seleccionar modelo grande para prompts largos")
    void testSelectLargeModelForLongPrompt() {
        // Arrange
        String userId = "user123";
        String longPrompt = "a".repeat(501); // Más de 500 caracteres

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Respuesta");

        // Act
        chatService.chat(userId, longPrompt);

        // Assert
        verify(groqClient).completeChat(anyList(), eq("llama-3.3-70b-versatile"));
    }

    @Test
    @DisplayName("Debería seleccionar modelo grande para prompts con palabras clave")
    void testSelectLargeModelForKeywordPrompts() {
        // Arrange
        String userId = "user123";
        String explainerPrompt = "Explica cómo funciona la autenticación";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Respuesta");

        // Act
        chatService.chat(userId, explainerPrompt);

        // Assert
        verify(groqClient).completeChat(anyList(), eq("llama-3.3-70b-versatile"));
    }

    @Test
    @DisplayName("Debería mantener orden de mensajes en conversación")
    void testMaintainMessageOrder() {
        // Arrange
        String userId = "user123";
        List<Map<String, String>> conversation = new ArrayList<>();
        conversation.add(Map.of("role", "user", "content", "Mensaje 1"));
        conversation.add(Map.of("role", "assistant", "content", "Respuesta 1"));

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>(conversation));
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Respuesta 2");

        // Act
        chatService.chat(userId, "Mensaje 2");

        // Assert
        verify(redisService).guardarConversacion(eq(userId), messagesCaptor.capture());
        List<Map<String, String>> saved = messagesCaptor.getValue();
        
        assertEquals("Mensaje 1", saved.get(0).get("content"));
        assertEquals("Respuesta 1", saved.get(1).get("content"));
        assertEquals("Mensaje 2", saved.get(2).get("content"));
        assertEquals("Respuesta 2", saved.get(3).get("content"));
    }

    @Test
    @DisplayName("Debería llamar a GroqClient con los mensajes correctos")
    void testCallGroqClientWithCorrectMessages() {
        // Arrange
        String userId = "user456";
        String userMessage = "¿Cuál es el significado de la vida?";
        List<Map<String, String>> emptyConversation = new ArrayList<>();

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(emptyConversation);
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("42");

        // Act
        chatService.chat(userId, userMessage);

        // Assert
        verify(groqClient).completeChat(messagesCaptor.capture(), anyString());
        List<Map<String, String>> capturedMessages = messagesCaptor.getValue();
        
        // Debe tener al menos el mensaje del usuario
        assertTrue(capturedMessages.stream().anyMatch(m ->
            "user".equals(m.get("role")) && userMessage.equals(m.get("content"))
        ));
    }

    @Test
    @DisplayName("Debería retornar respuesta de GroqClient")
    void testReturnGroqResponse() {
        // Arrange
        String userId = "user789";
        String expectedResponse = "Esta es la respuesta esperada";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn(expectedResponse);

        // Act
        String result = chatService.chat(userId, "Test");

        // Assert
        assertEquals(expectedResponse, result);
    }

    @Test
    @DisplayName("Debería manejar múltiples chats del mismo usuario")
    void testMultipleChatsFromSameUser() {
        // Arrange
        String userId = "user123";

        when(redisService.obtenerConversacion(eq(userId)))
                .thenReturn(new ArrayList<>())
                .thenReturn(new ArrayList<>(List.of(
                    Map.of("role", "user", "content", "Primer chat"),
                    Map.of("role", "assistant", "content", "Primera respuesta")
                )));
        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("Primera respuesta")
                .thenReturn("Segunda respuesta");

        // Act
        String firstResponse = chatService.chat(userId, "Primer chat");
        String secondResponse = chatService.chat(userId, "Segundo chat");

        // Assert
        assertEquals("Primera respuesta", firstResponse);
        assertEquals("Segunda respuesta", secondResponse);
        verify(redisService, times(2)).obtenerConversacion(eq(userId));
        verify(redisService, times(2)).guardarConversacion(eq(userId), anyList());
    }

    @Test
    @DisplayName("Debería manejar prompt case-insensitive para selección de modelo")
    void testModelSelectionCaseInsensitive() {
        // Arrange
        String userId = "user123";
        String uppercaseKeyword = "EXPLICA cómo funciona";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Respuesta");

        // Act
        chatService.chat(userId, uppercaseKeyword);

        // Assert
        verify(groqClient).completeChat(anyList(), eq("llama-3.3-70b-versatile"));
    }

    @Test
    @DisplayName("Debería preservar contenido del mensaje en Redis")
    void testPreserveMessageContent() {
        // Arrange
        String userId = "user123";
        String complexPrompt = "Analiza este código: var x = 1;";

        when(redisService.obtenerConversacion(eq(userId))).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Análisis");

        // Act
        chatService.chat(userId, complexPrompt);

        // Assert
        verify(redisService).guardarConversacion(eq(userId), messagesCaptor.capture());
        List<Map<String, String>> saved = messagesCaptor.getValue();
        
        assertTrue(saved.stream().anyMatch(m -> 
            complexPrompt.equals(m.get("content"))
        ));
    }
    
    @Test
    void shouldCallIAWhenCacheEmpty() {
        when(redisService.obtenerConversacion(anyString()))
                .thenReturn(new ArrayList<>());

        when(groqClient.completeChat(anyList(), anyString()))
                .thenReturn("respuesta");

        chatService.chat("user", "hola");

        verify(groqClient, times(1)).completeChat(anyList(), anyString());
    }
}
