package com.spring.ia.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import com.spring.ia.client.GroqClient;

/**
 * Servicio de chat que orquesta la conversación.
 * Responsabilidades:
 * - Gestión de estado de conversación (Redis)
 * - Lógica de negocio (selección de modelo, resumen, etc)
 * - Manejo de usuarios
 */
@Service
public class ChatService {

    private static final int MAX_MENSAJES = 10;
    private final GroqClient groqClient;
    private final RedisService redisService;

    public ChatService(GroqClient groqClient, RedisService redisService) {
        this.groqClient = groqClient;
        this.redisService = redisService;
    }

    public String chat(String userId, String prompt) {
        // 1. Traer conversación desde Redis
        List<Map<String, String>> mensajes = redisService.obtenerConversacion(userId);

        // 2. Agregar mensaje del usuario
        mensajes.add(Map.of("role", "user", "content", prompt));

        // 3. Preparar mensajes (resumen + límite)
        mensajes = prepararMensajes(mensajes);

        // 4. Llamar IA
        String respuesta = llamarIA(mensajes, prompt);

        // 5. Guardar respuesta
        mensajes.add(Map.of("role", "assistant", "content", respuesta));

        // 6. Guardar en Redis
        redisService.guardarConversacion(userId, mensajes);
        return respuesta;
    }

    private String llamarIA(List<Map<String, String>> mensajes, String prompt) {
        String model = elegirModelo(prompt);
        return groqClient.completeChat(mensajes, model);
    }

    private String elegirModelo(String prompt) {
        prompt = prompt.toLowerCase();
        if (prompt.length() > 500) return "llama-3.3-70b-versatile";
        if (prompt.contains("explica") ||
            prompt.contains("analiza") ||
            prompt.contains("por que") ||
            prompt.contains("arquitectura") ||
            prompt.contains("optimizar")) {
            return "llama-3.3-70b-versatile";
        }

        return "llama-3.1-8b-instant";
    }

    private List<Map<String, String>> prepararMensajes(List<Map<String, String>> mensajes) {
        if (mensajes.size() <= MAX_MENSAJES) {
            return mensajes;
        }

        // 1. Generar resumen con IA
        String resumen = generarResumenIA(mensajes);

        // 2. Tomar últimos N mensajes
        List<Map<String, String>> ultimos = mensajes.subList(
            mensajes.size() - MAX_MENSAJES,
            mensajes.size()
        );
        List<Map<String, String>> nuevos = new ArrayList<>(ultimos);

        // 3. Insertar resumen como system
        Map<String, String> resumenMsg = Map.of(
            "role", "system",
            "content", resumen
        );
        nuevos.add(0, resumenMsg);
        return nuevos;
    }

    private String generarResumenIA(List<Map<String, String>> mensajes) {
        String textoPlano = mensajes.stream()
            .map(m -> m.get("role") + ": " + m.get("content"))
            .reduce("", (a, b) -> a + "\n" + b);
        String prompt = "Resumí esta conversación en pocas líneas:\n" + textoPlano;
        return llamarIA(
            List.of(Map.of("role", "user", "content", prompt)),
            prompt
        );
    }
}