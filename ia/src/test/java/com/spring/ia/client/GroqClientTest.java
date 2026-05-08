package com.spring.ia.client;

import com.spring.ia.exception.GroqClientException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroqClient Tests")
class GroqClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private GroqClient groqClient;

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        groqClient = new GroqClient(builder, "https://api.groq.com/openai", "test-key");
    }

    @Test
    @DisplayName("Debería completar chat exitosamente con respuesta válida")
    void testCompleteChatSuccess() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Hola")
        );
        String model = "llama-3.1-70b-versatile";
        String groqResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Hola, ¿cómo estás?"
                        }
                    }]
                }
                """;

        setupWebClientMocks(groqResponse);

        // Act
        String result = groqClient.completeChat(messages, model);

        // Assert
        assertEquals("Hola, ¿cómo estás?", result);
        verify(webClient).post();
    }

    @Test
    @DisplayName("Debería manejar conversación multi-turno correctamente")
    void testCompleteChatMultiTurn() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "¿Cuál es tu nombre?"),
                Map.of("role", "assistant", "content", "Soy Groq"),
                Map.of("role", "user", "content", "¿Qué puedes hacer?")
        );
        String model = "llama-3.1-8b-instant";
        String groqResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Puedo ayudarte con análisis, código y más"
                        }
                    }]
                }
                """;

        setupWebClientMocks(groqResponse);

        // Act
        String result = groqClient.completeChat(messages, model);

        // Assert
        assertEquals("Puedo ayudarte con análisis, código y más", result);
    }

    @Test
    @DisplayName("Debería seleccionar modelo correcto en la solicitud")
    void testCompleteChatVerifyModelInRequest() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Test")
        );
        String model = "llama-3.3-70b-versatile";
        String groqResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "Response"
                        }
                    }]
                }
                """;

        setupWebClientMocks(groqResponse);

        // Act
        groqClient.completeChat(messages, model);

        // Assert
        verify(requestBodySpec).bodyValue(argThat(body -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) body;
            return map.get("model").equals(model);
        }));
    }

    @Test
    @DisplayName("Debería lanzar GroqClientException si hay error en la respuesta")
    void testCompleteChatInvalidResponse() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Test")
        );
        setupWebClientMocksFail("{\"error\": \"Invalid response\"}");

        // Act & Assert
        assertThrows(GroqClientException.class, () ->
                groqClient.completeChat(messages, "test-model")
        );
    }

    @Test
    @DisplayName("Debería manejar respuesta JSON malformada")
    void testCompleteChatMalformedJson() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Test")
        );
        setupWebClientMocks("{ invalid json }");

        // Act & Assert
        GroqClientException exception = assertThrows(
                GroqClientException.class,
                () -> groqClient.completeChat(messages, "test-model")
        );
        assertTrue(exception.getMessage().contains("parseando"));
    }

    @Test
    @DisplayName("Debería lanzar GroqClientException si falla conexión")
    void testCompleteChatNetworkError() {
        // Arrange
        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        when(webClient.post()).thenThrow(new RuntimeException("Network error"));

        groqClient = new GroqClient(builder, "https://api.groq.com/openai", "test-key");

        // Act & Assert
        assertThrows(GroqClientException.class, () ->
                groqClient.completeChat(
                        List.of(Map.of("role", "user", "content", "Test")),
                        "test-model"
                )
        );
    }

    @Test
    @DisplayName("Debería extraer contenido correctamente de respuesta anidada")
    void testExtractResponseFromNestedJson() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "Test")
        );
        String complexResponse = """
                {
                    "id": "chatcmpl-123",
                    "object": "chat.completion",
                    "created": 1234567890,
                    "model": "llama-3.1-70b-versatile",
                    "choices": [
                        {
                            "index": 0,
                            "message": {
                                "role": "assistant",
                                "content": "La respuesta extraída"
                            },
                            "finish_reason": "stop"
                        }
                    ],
                    "usage": {
                        "prompt_tokens": 10,
                        "completion_tokens": 5
                    }
                }
                """;

        setupWebClientMocks(complexResponse);

        // Act
        String result = groqClient.completeChat(messages, "test-model");

        // Assert
        assertEquals("La respuesta extraída", result);
    }

    @Test
    @DisplayName("Debería manejar respuesta con caracteres especiales")
    void testCompleteChatSpecialCharacters() {
        // Arrange
        List<Map<String, String>> messages = List.of(
                Map.of("role", "user", "content", "¿Cómo está?")
        );
        String groqResponse = """
                {
                    "choices": [{
                        "message": {
                            "content": "¡Bien! 😊 Aquí está: <code>var x = 1;</code>"
                        }
                    }]
                }
                """;

        setupWebClientMocks(groqResponse);

        // Act
        String result = groqClient.completeChat(messages, "test-model");

        // Assert
        assertTrue(result.contains("😊"));
        assertTrue(result.contains("<code>"));
    }

    // Helper methods

    private void setupWebClientMocks(String responseBody) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(responseBody));
    }

    private void setupWebClientMocksFail(String responseBody) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Simular respuesta con índice 0 pero sin choices válidos
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just("{\"choices\": []}"));
    }
}
