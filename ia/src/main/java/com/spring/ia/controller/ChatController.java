package com.spring.ia.controller;

import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.ChatRequest;
import com.spring.ia.dto.ChatResponse;
import com.spring.ia.service.ChatService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest input,
                                HttpSession session) {
        log.info("POST /api/chat recibido");
        String response = chatService.chat(input, session);
        return new ChatResponse(response);
    }
}
