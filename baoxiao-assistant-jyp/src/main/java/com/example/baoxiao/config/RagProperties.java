package com.example.baoxiao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.rag")
public class RagProperties {

    /** keyword（默认，离线可用） 或 vector（需本地 embedding 服务） */
    private String retriever = "keyword";

    /** 检索返回的最大制度片段数 */
    private int topK = 5;

    public String getRetriever() {
        return retriever;
    }

    public void setRetriever(String retriever) {
        this.retriever = retriever;
    }

    public int getTopK() {
        return topK;
    }

    public void setTopK(int topK) {
        this.topK = topK;
    }
}
