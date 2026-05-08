package com.spring.ia.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import com.spring.ia.dto.UserInput;
import com.spring.ia.service.ChatService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, String> chat(@Valid @RequestBody UserInput input) {
        log.info("Entró al endpoint chat");
        String response = chatService.chat(
                input.userId(),
                input.prompt()
        );
        log.info("Response final: {}", response);
        Map<String, String> result = new HashMap<>();
        result.put("response", response != null ? response : "");
        return result;
    }
}
