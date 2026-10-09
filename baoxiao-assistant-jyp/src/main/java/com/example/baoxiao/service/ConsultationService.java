package com.example.baoxiao.service;

import com.example.baoxiao.domain.Policy;
import com.example.baoxiao.dto.ConsultResponse;
import com.example.baoxiao.llm.LlmClient;
import com.example.baoxiao.llm.LlmMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 制度问答服务（员工自助查「有效制度」）。
 *
 * <p>链路：有效制度范围内检索 → 组织提示词 → LLM 归纳 → 返回带制度来源的答复。
 * 检索与回答都严格限定在「有效制度」内，草稿与已归档制度不会出现在员工视野里。</p>
 */
@Service
public class ConsultationService {

    private static final String SYSTEM_PROMPT =
            "你是企业报销制度问答助手。只能依据给定的有效制度片段作答，不得编造。";

    private final PolicyService policyService;
    private final LlmClient llmClient;
    private final RetryExecutor retryExecutor = new RetryExecutor();

    public ConsultationService(PolicyService policyService, LlmClient llmClient) {
        this.policyService = policyService;
        this.llmClient = llmClient;
    }

    public ConsultResponse consult(String question) {
        List<PolicyService.Retrieved> hits =
                retryExecutor.execute("policy.retrieve", 1, () -> policyService.retrieveValid(question, policyService.topK()));

        LocalDate today = LocalDate.now();
        if (hits.isEmpty()) {
            return new ConsultResponse(
                    "抱歉，未在公司现行有效制度中检索到与该问题相关的条款。"
                            + "请调整关键词后重试；如确属制度空白，可联系行政专员乔姐评估是否需要补充或修订制度。",
                    today, 0, new ArrayList<>());
        }

        String prompt = buildPrompt(question, hits);
        String answer = retryExecutor.execute("llm.answer", 1, () -> llmClient.chat(List.of(
                new LlmMessage("system", SYSTEM_PROMPT),
                new LlmMessage("user", prompt)
        )));

        return new ConsultResponse(answer, today, hits.size(), buildSources(hits));
    }

    private String buildPrompt(String question, List<PolicyService.Retrieved> hits) {
        StringBuilder sb = new StringBuilder();
        sb.append("请依据下面提供的【有效制度片段】回答员工问题，只依据制度内容，不臆造。\n\n");
        sb.append("【有效制度片段】\n");
        for (PolicyService.Retrieved hit : hits) {
            Policy policy = hit.getPolicy();
            sb.append("- [").append(policy.getCode()).append(" v").append(policy.getVersion()).append("] ")
                    .append(hit.getContent()).append("\n");
        }
        sb.append("\n【员工问题】\n").append(question.trim()).append("\n");
        return sb.toString();
    }

    private List<ConsultResponse.Source> buildSources(List<PolicyService.Retrieved> hits) {
        Map<Long, ConsultResponse.Source> sources = new LinkedHashMap<>();
        for (PolicyService.Retrieved hit : hits) {
            Policy policy = hit.getPolicy();
            sources.putIfAbsent(policy.getId(), new ConsultResponse.Source(
                    policy.getId(), policy.getCode(), policy.getTitle(), policy.getVersion(), policy.getCategory()));
        }
        return new ArrayList<>(sources.values());
    }
}
