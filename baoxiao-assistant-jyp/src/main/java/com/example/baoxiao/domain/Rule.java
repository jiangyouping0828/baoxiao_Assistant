package com.example.baoxiao.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * 额度规则：某类费用的最高报销额度。由行政专员维护，问答时用于确定性裁决。
 */
@Entity
@Table(name = "rule")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String category;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal max;

    protected Rule() {
    }

    public Rule(String category, BigDecimal max) {
        this.category = category;
        this.max = max;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMax() {
        return max;
    }

    public void setMax(BigDecimal max) {
        this.max = max;
    }
}
