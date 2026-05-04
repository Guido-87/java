package com.spring.ia.controller;

import com.spring.ia.service.ChatService;
import com.spring.ia.controller.ChatController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ChatService chatService;

    @Test
    void testChat() throws Exception {
        when(chatService.chat("user123", "What is AI?")).thenReturn("AI response");

        mockMvc.perform(post("/api/chat")
                .contentType("application/json")
                .content("{\"prompt\":\"What is AI?\",\"userId\":\"user123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("AI response"));
    }
}
