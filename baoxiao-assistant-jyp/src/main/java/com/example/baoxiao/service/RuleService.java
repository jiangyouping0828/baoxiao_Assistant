package com.example.baoxiao.service;

import com.example.baoxiao.domain.Rule;
import com.example.baoxiao.llm.LlmClient;
import com.example.baoxiao.llm.LlmMessage;
import com.example.baoxiao.repository.RuleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 额度规则服务：行政专员用自然语言维护「某类费用最高报销额度」。
 * LLM 只负责结构化理解，最终额度判断由本服务的确定性逻辑完成。
 */
@Service
public class RuleService {

    private final RuleRepository ruleRepository;
    private final LlmClient llmClient;
    private final ObjectMapper objectMapper;
    private final RetryExecutor retryExecutor = new RetryExecutor();

    public RuleService(RuleRepository ruleRepository, LlmClient llmClient, ObjectMapper objectMapper) {
        this.ruleRepository = ruleRepository;
        this.llmClient = llmClient;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public List<Rule> parseAndReplace(String text) {
        String prompt = "请解析业务规则，严格只返回 JSON 数组，元素形如 {\"category\":\"类别\",\"max\":额度数字}。\n"
                + "规则文本：" + text;
        String response = retryExecutor.execute("llm.parseRules", 1, () -> llmClient.chat(List.of(
                new LlmMessage("system", "你只输出合法 JSON，不要解释。"),
                new LlmMessage("user", prompt)
        )));
        List<Rule> parsed = readRules(response);
        ruleRepository.deleteAll();
        return ruleRepository.saveAll(parsed);
    }

    public List<Rule> listRules() {
        return new ArrayList<>(ruleRepository.findAll());
    }

    public Optional<Rule> findByCategory(String category) {
        if (category == null) {
            return Optional.empty();
        }
        return ruleRepository.findByCategory(category);
    }

    @Transactional
    public Rule saveRule(String category, BigDecimal max) {
        Optional<Rule> existing = ruleRepository.findByCategory(category);
        if (existing.isPresent()) {
            Rule rule = existing.get();
            rule.setMax(max);
            return ruleRepository.save(rule);
        }
        return ruleRepository.save(new Rule(category, max));
    }

    private List<Rule> readRules(String response) {
        try {
            List<RulePayload> payloads = objectMapper.readValue(
                    JsonExtractor.extractArray(response), new TypeReference<List<RulePayload>>() {
                    });
            List<Rule> rules = new ArrayList<>();
            for (RulePayload payload : payloads) {
                if (payload.category == null || payload.category.trim().isEmpty() || payload.max == null) {
                    continue;
                }
                rules.add(new Rule(payload.category.trim(), payload.max));
            }
            if (rules.isEmpty()) {
                throw BizException.badRequest("未能从输入中解析出任何额度规则，请使用「类别+不超过/最高+金额」的表述。");
            }
            return rules;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw BizException.badRequest("规则解析失败，请检查输入格式。");
        }
    }

    /** 供 Jackson 反序列化使用。 */
    public static class RulePayload {
        public String category;
        public BigDecimal max;
    }
}
