package com.spring.ia;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatGPTController {
    private final ChatService chatService;

    public ChatGPTController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/hola")
    public String hola() {
        return "Hola";
    }

    @GetMapping("/consulta")
    public String consulta(@RequestParam String entrada) {
        return chatService.consulta(entrada);
    }

    @PostMapping("/consulta")
    public String consultaPost(@RequestBody UserInput input) {
        return chatService.consulta(input.consulta());
    }
}
