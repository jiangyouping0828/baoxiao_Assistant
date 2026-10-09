package com.example.baoxiao.domain;

/**
 * 系统角色。
 *
 * <p>EMPLOYEE 员工：自助查询有效制度、经授权查询本人单据；
 * ADMIN    行政专员（乔姐）：维护制度（新增/修订/发布/归档）；
 * FINANCE  财务专员（周敏）：核对单据业务事实。</p>
 */
public enum UserRole {

    EMPLOYEE("员工"),
    ADMIN("行政专员"),
    FINANCE("财务专员");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
