package com.example.baoxiao.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 报销单据。归属某位员工（{@link #ownerUsername}），员工只能查询本人单据。
 * 业务事实由财务专员（周敏）核对并留痕。
 */
@Entity
@Table(name = "expense_doc")
public class ExpenseDoc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String docNo;

    /** 单据归属人（员工登录名），授权查询的唯一依据 */
    @Column(nullable = false, length = 64)
    private String ownerUsername;

    /** 申请人姓名 */
    @Column(nullable = false, length = 64)
    private String applicantName;

    @Column(nullable = false, length = 64)
    private String category;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    @Column(length = 500)
    private String summary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DocStatus status = DocStatus.SUBMITTED;

    /** 业务事实是否已由财务专员核对通过 */
    @Column(nullable = false)
    private boolean factVerified = false;

    /** 核对人（财务专员） */
    @Column(length = 64)
    private String verifiedBy;

    @Column(length = 500)
    private String verifyNote;

    private LocalDateTime verifiedAt;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    protected ExpenseDoc() {
    }

    public ExpenseDoc(String docNo, String ownerUsername, String applicantName, String category,
                      BigDecimal amount, LocalDate expenseDate, String summary) {
        this.docNo = docNo;
        this.ownerUsername = ownerUsername;
        this.applicantName = applicantName;
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.summary = summary;
    }

    public Long getId() {
        return id;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(LocalDate expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DocStatus getStatus() {
        return status;
    }

    public void setStatus(DocStatus status) {
        this.status = status;
    }

    public boolean isFactVerified() {
        return factVerified;
    }

    public void setFactVerified(boolean factVerified) {
        this.factVerified = factVerified;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public String getVerifyNote() {
        return verifyNote;
    }

    public void setVerifyNote(String verifyNote) {
        this.verifyNote = verifyNote;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
