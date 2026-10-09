package com.example.baoxiao.config;

import com.example.baoxiao.llm.HttpLlmClient;
import com.example.baoxiao.llm.LlmClient;
import com.example.baoxiao.llm.MockLlmClient;
import com.example.baoxiao.llm.OllamaLlmClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class LlmConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public LlmClient llmClient(LlmProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        if (properties.isMock()) {
            return new MockLlmClient();
        }
        if ("ollama".equalsIgnoreCase(properties.getProvider())) {
            return new OllamaLlmClient(properties, restTemplate, objectMapper);
        }
        return new HttpLlmClient(properties, restTemplate, objectMapper);
    }
}
