package com.spring.ia.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LanguageDetectorTest {
    @Test
    void detectsEnglishPromptsWithoutThePreviouslyRequiredKeywords() {
        assertThat(LanguageDetector.detect("Please explain virtual threads in Java")).isEqualTo("en");
        assertThat(LanguageDetector.detect("Summarize all classes")).isEqualTo("en");
    }

    @Test
    void keepsSpanishPromptsInSpanish() {
        assertThat(LanguageDetector.detect("¿Podés explicar los hilos virtuales?" )).isEqualTo("es");
    }

    @Test
    void defaultsBlankPromptsToSpanish() {
        assertThat(LanguageDetector.detect("  ")).isEqualTo("es");
    }
}
