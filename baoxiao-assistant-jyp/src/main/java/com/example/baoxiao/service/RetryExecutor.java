package com.example.baoxiao.service;

import java.util.function.Supplier;

/**
 * 工具调用重试器：LLM / 检索失败时按次数重试，避免偶发失败直接击穿链路。
 */
public class RetryExecutor {

    public <T> T execute(String actionName, int maxRetries, Supplier<T> supplier) {
        RuntimeException last = null;
        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                last = e;
                System.err.println("Tool failed: " + actionName + ", attempt=" + (attempt + 1) + ", reason=" + e.getMessage());
            }
        }
        throw last;
    }
}
