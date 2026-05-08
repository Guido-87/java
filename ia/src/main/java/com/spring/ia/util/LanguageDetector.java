package com.spring.ia.util;

public class LanguageDetector {

    public static String detect(String text) {
        if (text == null || text.isBlank()) {
            return "es";
        }
        text = text.toLowerCase();
        if (text.matches(".*\\b(the|and|what|how|why|hello|thanks)\\b.*")) {
            return "en";
        }
        return "es";
    }
}