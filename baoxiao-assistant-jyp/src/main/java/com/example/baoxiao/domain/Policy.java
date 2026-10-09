package com.example.baoxiao.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报销制度。由行政专员（乔姐）维护。
 *
 * <p>「有效制度」= 状态为 PUBLISHED 且 当前日期落在 [effectiveFrom, effectiveTo] 区间内。</p>
 */
@Entity
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 制度编号，如 BX-TRAVEL-2026 */
    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String title;

    /** 归口类别：差旅/交通/餐饮/办公/招待… */
    @Column(nullable = false, length = 64)
    private String category;

    @Lob
    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private int version = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PolicyStatus status = PolicyStatus.DRAFT;

    @Column(nullable = false)
    private LocalDate effectiveFrom = LocalDate.now();

    /** 为空表示长期有效 */
    private LocalDate effectiveTo;

    /** 维护人（行政专员） */
    @Column(length = 64)
    private String maintainedBy;

    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected Policy() {
    }

    public Policy(String code, String title, String category, String content, String maintainedBy) {
        this.code = code;
        this.title = title;
        this.category = category;
        this.content = content;
        this.maintainedBy = maintainedBy;
    }

    /** 当前是否有效：已发布 + 在生效期内。 */
    public boolean isValidOn(LocalDate date) {
        if (status != PolicyStatus.PUBLISHED) {
            return false;
        }
        if (effectiveFrom != null && date.isBefore(effectiveFrom)) {
            return false;
        }
        return effectiveTo == null || !date.isAfter(effectiveTo);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public PolicyStatus getStatus() {
        return status;
    }

    public void setStatus(PolicyStatus status) {
        this.status = status;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getMaintainedBy() {
        return maintainedBy;
    }

    public void setMaintainedBy(String maintainedBy) {
        this.maintainedBy = maintainedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
