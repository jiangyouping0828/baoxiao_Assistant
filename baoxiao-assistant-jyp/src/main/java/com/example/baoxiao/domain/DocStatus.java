package com.example.baoxiao.domain;

/**
 * 单据状态。
 *
 * <p>SUBMITTED 已提交：员工提交，等待财务核对；
 * CHECKING  核对中：财务专员已受理；
 * VERIFIED  事实已核对：业务事实经财务专员周敏确认无误；
 * REJECTED  事实有误：核对发现业务事实与单据不符。</p>
 */
public enum DocStatus {

    SUBMITTED("已提交"),
    CHECKING("核对中"),
    VERIFIED("事实已核对"),
    REJECTED("事实有误");

    private final String label;

    DocStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
