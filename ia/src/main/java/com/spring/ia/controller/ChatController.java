package com.spring.ia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.UserInput;
import com.spring.ia.service.ChatService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(@Valid @RequestBody UserInput input) {
        return chatService.chat(input.userId(), input.prompt());
    }
}
