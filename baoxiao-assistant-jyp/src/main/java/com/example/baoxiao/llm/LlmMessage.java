package com.example.baoxiao.llm;

public class LlmMessage {

    private final String role;
    private final String content;

    public LlmMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}
