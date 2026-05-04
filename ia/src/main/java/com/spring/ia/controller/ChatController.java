package com.spring.ia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.UserInput;
import com.spring.ia.service.ChatService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public String chat(@RequestBody UserInput input) {
        return chatService.chat(input.userId(), input.prompt());
    }
}
