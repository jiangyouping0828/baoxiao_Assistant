package com.example.baoxiao.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 制度切片：制度正文按句/换行拆分后入库，供关键词检索（轻量 RAG）使用。
 * 检索时按 {@link #policyId} 关联的制度是否有效进行过滤。
 */
@Entity
@Table(name = "policy_chunk")
public class PolicyChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long policyId;

    @Lob
    @Column(nullable = false)
    private String content;

    /** 预抽取的关键词（含中文 bigram），以逗号分隔 */
    @Column(length = 2000)
    private String keywords;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected PolicyChunk() {
    }

    public PolicyChunk(Long policyId, String content, String keywords) {
        this.policyId = policyId;
        this.content = content;
        this.keywords = keywords;
    }

    public Long getId() {
        return id;
    }

    public Long getPolicyId() {
        return policyId;
    }

    public String getContent() {
        return content;
    }

    public String getKeywords() {
        return keywords;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
