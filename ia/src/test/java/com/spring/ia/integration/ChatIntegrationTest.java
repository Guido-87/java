package com.spring.ia.integration;

import com.spring.ia.client.GroqClient;
import com.spring.ia.service.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;

@SpringBootTest
@AutoConfigureMockMvc
class ChatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GroqClient groqClient;

    @MockitoBean
    private RedisService redisService;

    @Test
    void flujoCompletoChat() throws Exception {
        when(redisService.obtenerConversacion(anyString())).thenReturn(new ArrayList<>());
        when(groqClient.completeChat(anyList(), anyString())).thenReturn("Hola, ¿cómo estás?");
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"1\",\"prompt\":\"hola\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(emptyString())));
    }
}