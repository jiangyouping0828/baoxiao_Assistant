package com.example.baoxiao.service;

public final class JsonExtractor {

    private JsonExtractor() {
    }

    public static String extractArray(String text) {
        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Cannot find JSON array in LLM response: " + text);
        }
        return text.substring(start, end + 1);
    }

    public static String extractObject(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Cannot find JSON object in LLM response: " + text);
        }
        return text.substring(start, end + 1);
    }
}
