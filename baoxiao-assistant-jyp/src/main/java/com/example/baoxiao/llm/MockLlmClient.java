package com.example.baoxiao.llm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 本地 Mock 大模型：不依赖任何外部服务，保证全流程可离线跑通。
 *
 * <p>它按提示词特征分派两类任务：
 * <ul>
 *   <li>制度问答：把提示词中检索到的【有效制度片段】整理成一段自然语言回答，并标注制度编号；</li>
 *   <li>规则解析：把自然语言额度规则解析为 JSON 数组。</li>
 * </ul>
 * 接入真实模型后，本类不再被使用（{@code ai.llm.mock=false}）。</p>
 */
public class MockLlmClient implements LlmClient {

    private static final Pattern SOURCE_PATTERN = Pattern.compile("\\[([A-Za-z0-9\\-]+)\\s+v(\\d+)\\]");
    /** 匹配检索片段行：- [BX-XXX v1] 正文 */
    private static final Pattern CHUNK_LINE_PATTERN =
            Pattern.compile("^-\\s*\\[[A-Za-z0-9\\-]+\\s+v\\d+\\]\\s*(.+)$");
    private static final Pattern RULE_PATTERN =
            Pattern.compile("(.+?)(?:报销)?(?:不超过|最高|上限|限额|封顶)(\\d+(?:\\.\\d+)?)");

    @Override
    public String chat(List<LlmMessage> messages) {
        String prompt = messages.get(messages.size() - 1).getContent();
        if (prompt.contains("有效制度片段")) {
            return answerPolicyQuestion(prompt);
        }
        if (prompt.contains("解析业务规则")) {
            return parseRules(prompt);
        }
        return "{}";
    }

    // ------------------------------------------------------------------
    // 制度问答
    // ------------------------------------------------------------------

    private String answerPolicyQuestion(String prompt) {
        // 取最后一次出现的位置：提示词首句也会提到「有效制度片段」，不能误取。
        int marker = prompt.lastIndexOf("【有效制度片段】");
        String chunks = marker < 0 ? "" : prompt.substring(marker + "【有效制度片段】".length());
        int questionIndex = chunks.indexOf("【员工问题】");
        if (questionIndex >= 0) {
            chunks = chunks.substring(0, questionIndex);
        }
        String question = extractAfter(prompt, "【员工问题】");
        if (chunks.trim().isEmpty()) {
            return "抱歉，未在公司现行有效制度中检索到与该问题相关的条款。"
                    + "建议联系行政专员乔姐确认制度口径，或由财务专员周敏核对具体单据。";
        }

        // 只保留真正的制度片段行（形如「- [BX-XXX v1] 正文」），剔除提示词模板残留。
        List<String> lines = new ArrayList<>();
        for (String raw : chunks.split("\\n")) {
            Matcher lineMatcher = CHUNK_LINE_PATTERN.matcher(raw.trim());
            if (lineMatcher.find()) {
                lines.add(lineMatcher.group(1).trim());
            }
        }

        Set<String> sources = new LinkedHashSet<>();
        Matcher matcher = SOURCE_PATTERN.matcher(chunks);
        while (matcher.find()) {
            sources.add(matcher.group(1) + "（第" + matcher.group(2) + "版）");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("依据公司现行有效制度，就「").append(question.trim()).append("」答复如下：\n");
        int limit = Math.min(lines.size(), 3);
        for (int i = 0; i < limit; i++) {
            sb.append("• ").append(stripSourceTag(lines.get(i))).append("\n");
        }
        if (limit < lines.size()) {
            sb.append("（另有 ").append(lines.size() - limit).append(" 条相关条款，可进一步细化提问）\n");
        }
        sb.append("\n制度依据：").append(sources.isEmpty() ? "现行有效制度" : String.join("、", sources)).append("。");
        sb.append("\n制度口径由行政专员乔姐负责解释与维护；具体单据的业务事实请以财务专员周敏的核对结论为准。");
        return sb.toString();
    }

    private String stripSourceTag(String line) {
        return line.replaceAll("^[-•]\\s*", "").replaceAll("^\\[[^\\]]+\\]\\s*", "").trim();
    }

    // ------------------------------------------------------------------
    // 额度规则解析
    // ------------------------------------------------------------------

    private String parseRules(String prompt) {
        String text = extractAfter(prompt, "规则文本：");
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        for (String statement : text.split("[，。；;\\n]+")) {
            Matcher matcher = RULE_PATTERN.matcher(statement.trim());
            if (matcher.find()) {
                if (!first) {
                    json.append(",");
                }
                String category = normalizeCategory(matcher.group(1));
                BigDecimal max = new BigDecimal(matcher.group(2));
                json.append("{\"category\":\"").append(category).append("\",\"max\":").append(max).append("}");
                first = false;
            }
        }
        json.append("]");
        return json.toString();
    }

    private String normalizeCategory(String value) {
        return value.replace("报销", "").replace("费用", "").trim();
    }

    private String extractAfter(String text, String marker) {
        int index = text.indexOf(marker);
        return index < 0 ? text : text.substring(index + marker.length());
    }
}
