package com.spring.ia.util;

import java.util.Locale;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LanguageDetector {
    private static final Set<String> ENGLISH_MARKERS = Set.of(
            "the", "and", "what", "how", "why", "hello", "hi", "thanks", "please", "can", "could",
            "would", "should", "does", "do", "are", "is", "was", "were", "explain", "write", "show",
            "create", "build", "implement", "fix", "review", "help", "with", "for", "this", "that",
            "from", "about", "into", "using", "have", "need", "want", "application", "project", "summarize",
            "summary", "summarized", "describe", "add", "change", "search", "list", "find", "compare", "tell", "get"
    );
    private static final Set<String> SPANISH_MARKERS = Set.of(
            "que", "qué", "cómo", "como", "hola", "gracias", "podés", "podes", "podrías", "podrias",
            "puedes", "quiero", "necesito", "explica", "explicame", "mostrá", "mostra", "mostrar", "crea",
            "crear", "arregla", "revisa", "con", "para", "este", "esta", "estos", "estas", "una", "uno",
            "los", "las", "del", "el", "la", "es", "son", "tengo", "hacer", "decime", "ayuda", "resumir",
            "resumí", "resumi", "describí", "describi", "agrega", "agregar", "busca", "buscar", "lista", "listar", "compara"
    );

    public static String detect(String text) {
        if (text == null || text.isBlank()) {
            return "es";
        }
        Set<String> words = new HashSet<>(Arrays.asList(text.toLowerCase(Locale.ROOT).split("[^\\p{L}]+")));
        long englishScore = ENGLISH_MARKERS.stream().filter(words::contains).count();
        long spanishScore = SPANISH_MARKERS.stream().filter(words::contains).count();
        return englishScore > spanishScore ? "en" : "es";
    }
}
