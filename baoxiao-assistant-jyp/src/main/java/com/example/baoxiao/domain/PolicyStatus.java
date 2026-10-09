package com.example.baoxiao.domain;

/**
 * 制度状态。
 *
 * <p>DRAFT     草稿：仅行政专员可见，不参与员工查询与问答；
 * PUBLISHED 已发布：在生效期内即为「有效制度」，员工可查；
 * ARCHIVED  已归档：历史版本，保留追溯，不参与查询。</p>
 */
public enum PolicyStatus {

    DRAFT("草稿"),
    PUBLISHED("已发布"),
    ARCHIVED("已归档");

    private final String label;

    PolicyStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
