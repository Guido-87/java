package com.spring.ia;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class ChatService {
    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder chatClient) {
        this.chatClient = chatClient.build();
    }

    public String consulta(String mensaje) {
        return chatClient.prompt().user(mensaje).call().content();
    }
}
