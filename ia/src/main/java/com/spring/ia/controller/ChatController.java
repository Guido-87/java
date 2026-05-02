package com.spring.ia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.UserInput;
import com.spring.ia.service.ChatService;

@Controller
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/hola")
    @ResponseBody
    public String hola() {
        return "<h1>Hola</h1>";
    }

    @GetMapping
    @ResponseBody
    public String chatTest(@RequestParam String entrada) {
        return chatService.chat("test-user", entrada);
    }

    @PostMapping
    @ResponseBody
    public String chat(@RequestBody UserInput input) {
        return chatService.chat(input.userId(), input.prompt());
    }
}
