package com.example.baoxiao.llm;

import java.util.List;

public interface LlmClient {

    String chat(List<LlmMessage> messages);
}
