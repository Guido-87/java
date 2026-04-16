package com.spring.ia.controller;

import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.UserInput;
import com.spring.ia.service.ChatService;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/hola")
    public String hola() {
        return "Hola";
    }

    @GetMapping("/chat")
    public String chatTest(@RequestParam String entrada) {
        return chatService.chat("test-user", entrada);
    }

    @PostMapping("/chat")
    public String chat(@RequestBody UserInput input) {
        return chatService.chat(input.userId(), input.prompt());
    }
}
