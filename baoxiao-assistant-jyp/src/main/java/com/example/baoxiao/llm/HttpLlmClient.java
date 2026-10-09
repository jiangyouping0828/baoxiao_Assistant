package com.example.baoxiao.llm;

import com.example.baoxiao.config.LlmProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAI 兼容接口客户端（智谱 GLM / DeepSeek / 通义 等均可）。
 */
public class HttpLlmClient implements LlmClient {

    private final LlmProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpLlmClient(LlmProperties properties, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public String chat(List<LlmMessage> messages) {
        RuntimeException lastError = null;
        for (int attempt = 0; attempt <= properties.getMaxRetries(); attempt++) {
            try {
                return doChat(messages);
            } catch (RuntimeException e) {
                lastError = e;
                System.err.println("LLM call failed, attempt=" + (attempt + 1));
            }
        }
        throw new LlmException("LLM call failed after retries", lastError);
    }

    private String doChat(List<LlmMessage> messages) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(properties.getApiKey());

        Map<String, Object> body = new HashMap<>();
        body.put("model", properties.getModel());
        body.put("messages", messages.stream().map(this::toMap).collect(Collectors.toList()));
        body.put("temperature", 0.1);

        try {
            String response = restTemplate.postForObject(properties.getBaseUrl(), new HttpEntity<>(body, headers), String.class);
            JsonNode root = objectMapper.readTree(response);
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (content.isMissingNode() || content.asText().trim().isEmpty()) {
                throw new LlmException("LLM response content is empty: " + response);
            }
            return content.asText();
        } catch (RestClientException e) {
            throw new LlmException("LLM HTTP error", e);
        } catch (Exception e) {
            throw new LlmException("LLM response parse error", e);
        }
    }

    private Map<String, String> toMap(LlmMessage message) {
        Map<String, String> map = new HashMap<>();
        map.put("role", message.getRole());
        map.put("content", message.getContent());
        return map;
    }
}
