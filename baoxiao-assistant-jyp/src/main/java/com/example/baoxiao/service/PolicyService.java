package com.example.baoxiao.service;

import com.example.baoxiao.config.RagProperties;
import com.example.baoxiao.domain.Policy;
import com.example.baoxiao.domain.PolicyChunk;
import com.example.baoxiao.domain.PolicyStatus;
import com.example.baoxiao.dto.PolicyForm;
import com.example.baoxiao.repository.PolicyChunkRepository;
import com.example.baoxiao.repository.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 制度服务。
 *
 * <p>职责边界：
 * <ul>
 *   <li>行政专员（乔姐）维护制度：新增 / 修订 / 发布 / 归档，修订自动升版本；</li>
 *   <li>员工只看得到「有效制度」（已发布且在生效期内）；</li>
 *   <li>为制度问答提供「仅限有效制度」的检索能力（轻量关键词 RAG）。</li>
 * </ul>
 * </p>
 */
@Service
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final PolicyChunkRepository chunkRepository;
    private final RagProperties ragProperties;

    public PolicyService(PolicyRepository policyRepository, PolicyChunkRepository chunkRepository,
                         RagProperties ragProperties) {
        this.policyRepository = policyRepository;
        this.chunkRepository = chunkRepository;
        this.ragProperties = ragProperties;
    }

    // ------------------------------------------------------------------
    // 查询
    // ------------------------------------------------------------------

    /** 全部制度（行政专员维护视图）。 */
    public List<Policy> listAll() {
        return policyRepository.findAllByOrderByUpdatedAtDesc();
    }

    /** 当前有效制度：已发布 + 在生效期内。员工据此自助查询。 */
    public List<Policy> listValid() {
        LocalDate today = LocalDate.now();
        return policyRepository.findAll().stream()
                .filter(policy -> policy.isValidOn(today))
                .sorted(Comparator.comparing(Policy::getCategory).thenComparing(Policy::getCode))
                .collect(Collectors.toList());
    }

    /** 某制度当前是否有效。 */
    public boolean isValid(Policy policy) {
        return policy != null && policy.isValidOn(LocalDate.now());
    }

    public Policy getById(Long id) {
        return policyRepository.findById(id)
                .orElseThrow(() -> BizException.notFound("制度不存在：" + id));
    }

    // ------------------------------------------------------------------
    // 维护（仅行政专员）
    // ------------------------------------------------------------------

    @Transactional
    public Policy createOrUpdate(PolicyForm form, String operator) {
        LocalDate from = form.getEffectiveFrom() == null ? LocalDate.now() : form.getEffectiveFrom();
        Optional<Policy> existing = policyRepository.findByCode(form.getCode());
        Policy policy;
        if (existing.isPresent()) {
            policy = existing.get();
            // 正文变化即视为修订，版本 +1
            if (!policy.getContent().equals(form.getContent())) {
                policy.setVersion(policy.getVersion() + 1);
            }
        } else {
            policy = new Policy(form.getCode(), form.getTitle(), form.getCategory(), form.getContent(), operator);
        }
        policy.setTitle(form.getTitle());
        policy.setCategory(form.getCategory());
        policy.setContent(form.getContent());
        policy.setEffectiveFrom(from);
        policy.setEffectiveTo(form.getEffectiveTo());
        policy.setMaintainedBy(operator);
        policy.setUpdatedAt(LocalDateTime.now());
        policy.setStatus(form.isPublish() ? PolicyStatus.PUBLISHED : PolicyStatus.DRAFT);

        Policy saved = policyRepository.save(policy);
        reindex(saved);
        return saved;
    }

    @Transactional
    public Policy publish(Long id, String operator) {
        Policy policy = getById(id);
        policy.setStatus(PolicyStatus.PUBLISHED);
        policy.setMaintainedBy(operator);
        policy.setUpdatedAt(LocalDateTime.now());
        Policy saved = policyRepository.save(policy);
        reindex(saved);
        return saved;
    }

    @Transactional
    public Policy archive(Long id, String operator) {
        Policy policy = getById(id);
        policy.setStatus(PolicyStatus.ARCHIVED);
        policy.setMaintainedBy(operator);
        policy.setUpdatedAt(LocalDateTime.now());
        return policyRepository.save(policy);
    }

    @Transactional
    public void delete(Long id) {
        chunkRepository.deleteByPolicyId(id);
        policyRepository.deleteById(id);
    }

    // ------------------------------------------------------------------
    // 检索（仅限有效制度）
    // ------------------------------------------------------------------

    /** 在「有效制度」范围内做关键词检索，返回带来源的命中片段。 */
    public List<Retrieved> retrieveValid(String query, int topK) {
        Map<Long, Policy> validPolicies = listValid().stream()
                .collect(Collectors.toMap(Policy::getId, Function.identity()));
        if (validPolicies.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> queryTerms = extractTerms(query);
        String normalized = query == null ? "" : query.trim();

        return chunkRepository.findAll().stream()
                .filter(chunk -> validPolicies.containsKey(chunk.getPolicyId()))
                .map(chunk -> {
                    Policy policy = validPolicies.get(chunk.getPolicyId());
                    return new Retrieved(policy, chunk.getContent(),
                            keywordScore(chunk, normalized, queryTerms));
                })
                .filter(retrieved -> retrieved.score > 0)
                .sorted(Comparator.comparingInt((Retrieved r) -> r.score).reversed())
                .limit(topK)
                .collect(Collectors.toList());
    }

    public int topK() {
        return ragProperties.getTopK();
    }

    // ------------------------------------------------------------------
    // 内部：切片与索引
    // ------------------------------------------------------------------

    private void reindex(Policy policy) {
        chunkRepository.deleteByPolicyId(policy.getId());
        List<PolicyChunk> chunks = new ArrayList<>();
        for (String part : splitChunks(policy.getContent())) {
            chunks.add(new PolicyChunk(policy.getId(), part, String.join(",", extractTerms(part))));
        }
        if (chunks.isEmpty()) {
            chunks.add(new PolicyChunk(policy.getId(), policy.getContent(), ""));
        }
        chunkRepository.saveAll(chunks);
    }

    private List<String> splitChunks(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null) {
            return chunks;
        }
        for (String part : text.split("[\\n\\u3002\\uff1b;]+")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                chunks.add(trimmed);
            }
        }
        return chunks;
    }

    private int keywordScore(PolicyChunk chunk, String query, Set<String> queryTerms) {
        int score = 0;
        String content = chunk.getContent();
        String keywords = chunk.getKeywords() == null ? "" : chunk.getKeywords();
        for (String term : queryTerms) {
            if (content.contains(term)) {
                score += 3;
            }
            if (keywords.contains(term)) {
                score += 2;
            }
        }
        if (!query.isEmpty() && content.contains(query)) {
            score += 5;
        }
        return score;
    }

    /** 抽取检索词：中文 bigram + 英文/数字 token。 */
    private Set<String> extractTerms(String text) {
        Set<String> terms = new LinkedHashSet<>();
        if (text == null) {
            return terms;
        }
        String cleaned = text.toLowerCase(Locale.ROOT)
                .replaceAll("[\\uFF0C\\u3002\\uFF1B;,.!?\\uFF01\\uFF1F\\u3001\\s]+", " ");
        for (String token : cleaned.split(" ")) {
            if (token.length() >= 2) {
                terms.add(token);
            }
        }
        for (int i = 0; i < text.length() - 1; i++) {
            char c1 = text.charAt(i);
            char c2 = text.charAt(i + 1);
            if (isCjk(c1) && isCjk(c2)) {
                terms.add("" + c1 + c2);
            }
        }
        return new HashSet<>(terms);
    }

    private boolean isCjk(char c) {
        return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
    }

    /** 检索命中结果：制度 + 片段 + 分值。 */
    public static class Retrieved {

        private final Policy policy;
        private final String content;
        private final int score;

        Retrieved(Policy policy, String content, int score) {
            this.policy = policy;
            this.content = content;
            this.score = score;
        }

        public Policy getPolicy() {
            return policy;
        }

        public String getContent() {
            return content;
        }

        public int getScore() {
            return score;
        }
    }
}
