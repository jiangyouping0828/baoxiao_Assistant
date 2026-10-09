package com.example.baoxiao.dto;

/**
 * 财务专员核对业务事实的请求体。
 */
public class VerifyRequest {

    /** true=事实核对通过，false=事实有误 */
    private boolean verified;

    private String note;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
