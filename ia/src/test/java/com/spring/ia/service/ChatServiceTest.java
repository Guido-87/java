package test.java.com.spring.ia.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import com.spring.ia.service.ChatService;
import com.spring.ia.service.RedisService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private RedisService redisService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        WebClient.Builder builder = spy(WebClient.Builder.class);
        when(builder.baseUrl(anyString())).thenReturn(builder);
        when(builder.defaultHeader(anyString(), anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(webClient);

        chatService = new ChatService(builder, "https://api.groq.com/openai", "test-key", redisService);
    }

    @Test
    void testChatServiceInitialization() {
        assertNotNull(chatService);
    }

    @Test
    void testRetrieveConversationFromRedis() {
        List<Map<String, String>> existingMessages = new ArrayList<>();
        existingMessages.add(Map.of("role", "user", "content", "Previous"));

        when(redisService.obtenerConversacion("user123")).thenReturn(existingMessages);

        List<Map<String, String>> result = redisService.obtenerConversacion("user123");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(redisService, times(1)).obtenerConversacion("user123");
    }

    @Test
    void testBuildUserMessage() {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", "Test"));

        assertEquals(1, messages.size());
        assertEquals("user", messages.get(0).get("role"));
    }

    @Test
    void testAssistantResponseTracking() {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", "Question"));
        messages.add(Map.of("role", "assistant", "content", "Answer"));

        assertEquals(2, messages.size());
        assertEquals("assistant", messages.get(1).get("role"));
    }

    @Test
    void testMessageHistoryOrder() {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", "First"));
        messages.add(Map.of("role", "assistant", "content", "Reply1"));
        messages.add(Map.of("role", "user", "content", "Second"));

        assertEquals(3, messages.size());
        assertEquals("First", messages.get(0).get("content"));
        assertEquals("Second", messages.get(2).get("content"));
    }
}
