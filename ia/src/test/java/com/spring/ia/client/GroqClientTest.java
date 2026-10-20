package com.spring.ia.client;

import com.spring.ia.exception.GroqClientException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import reactor.core.scheduler.Schedulers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(OutputCaptureExtension.class)
@DisplayName("GroqClient Tests")
class GroqClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @SuppressWarnings("rawtypes")
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

        setupWebClientMocks("{\"choices\": []}");

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

    @Test
    @DisplayName("Registra intentos sin exponer mensajes ni respuesta del proveedor")
    void logsTimingWithoutExposingConversation(CapturedOutput output) {
        String privatePrompt = "prompt-confidencial-7f31";
        String privateAnswer = "respuesta-confidencial-91ab";
        setupWebClientMocks("""
                {"choices":[{"message":{"content":"respuesta-confidencial-91ab"}}]}
                """);

        MDC.put("requestId", "request-test-123");
        try {
            groqClient.completeChat(List.of(Map.of("role", "user", "content", privatePrompt)), "test-model");
        } finally {
            MDC.remove("requestId");
        }

        assertFalse(output.getOut().contains(privatePrompt));
        assertFalse(output.getOut().contains(privateAnswer));
        assertTrue(output.getOut().contains("attempt=1"));
        assertTrue(output.getOut().contains("requestId=request-test-123"));
        assertTrue(output.getOut().contains("durationMs="));
    }

    @Test
    @DisplayName("Registra la causa del reintento y conserva el requestId entre hilos reactivos")
    void logsRetryReasonAndStableRequestId(CapturedOutput output) {
        String requestId = "retry-request-456";
        String privatePrompt = "retry-prompt-secret";
        String privateResponse = "retry-answer-secret";
        String privateFailure = "retry-exception-secret";
        String successBody = """
                {"choices":[{"message":{"content":"retry-answer-secret"}}]}
                """;
        AtomicInteger responseCalls = new AtomicInteger();
        setupWebClientMocks(successBody);
        when(responseSpec.bodyToMono(String.class)).thenAnswer(invocation -> {
            if (responseCalls.getAndIncrement() == 0) {
                return Mono.<String>error(new TimeoutException(privateFailure))
                        .publishOn(Schedulers.parallel());
            }
            return Mono.just(successBody).publishOn(Schedulers.parallel());
        });

        MDC.put("requestId", requestId);
        String result;
        try {
            result = groqClient.completeChat(
                    List.of(Map.of("role", "user", "content", privatePrompt)), "test-model");
        } finally {
            MDC.remove("requestId");
        }

        assertEquals("retry-answer-secret", result);
        assertTrue(output.getOut().contains("outcome=error"));
        assertTrue(output.getOut().contains("retry reason=timeout"));
        assertTrue(output.getOut().contains(requestId));
        assertFalse(output.getOut().contains(privatePrompt));
        assertFalse(output.getOut().contains(privateResponse));
        assertFalse(output.getOut().contains(privateFailure));
        verify(responseSpec, times(2)).bodyToMono(String.class);
    }

    @Test
    @DisplayName("No reintenta errores HTTP del proveedor")
    void doesNotRetryUpstreamHttpErrors() {
        setupWebClientMocks("unused");
        when(responseSpec.bodyToMono(String.class)).thenReturn(
                Mono.error(new GroqClientException("Groq respondió con estado HTTP 400")));

        assertThrows(GroqClientException.class, () -> groqClient.completeChat(
                List.of(Map.of("role", "user", "content", "Hola")), "test-model"));

        verify(responseSpec, times(1)).bodyToMono(String.class);
    }

    @SuppressWarnings({"unchecked"})
    private void setupWebClientMocks(String responseBody) {
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));
    }
}
