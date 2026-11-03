package com.spring.ia.exception;

import com.spring.ia.controller.ChatController;
import com.spring.ia.service.ChatService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ChatController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @MockitoBean
    private CacheManager cacheManager;

    @Test
    void returns500WithSpanishMessage() throws Exception {
        when(chatService.chat(any(), any()))
                .thenThrow(new RuntimeException("boom"));

        mockMvc.perform(post("/api/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"1\",\"prompt\":\"hola\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ocurrió un problema. Intentá de nuevo más tarde."));
    }

    @Test
    void doesNotWriteJsonWhenAnSseConnectionFails() throws Exception {
        when(chatService.chat(any(), any()))
                .thenThrow(new RuntimeException("stream disconnected"));

        mockMvc.perform(post("/api/chat")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":\"1\",\"prompt\":\"hola\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(""));
    }
}
