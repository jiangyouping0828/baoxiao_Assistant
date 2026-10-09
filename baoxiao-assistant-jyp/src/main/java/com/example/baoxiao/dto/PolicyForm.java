package com.example.baoxiao.dto;

import javax.validation.constraints.NotBlank;

import java.time.LocalDate;

/**
 * 行政专员维护制度时的表单。
 */
public class PolicyForm {

    @NotBlank(message = "制度编号不能为空")
    private String code;

    @NotBlank(message = "制度标题不能为空")
    private String title;

    @NotBlank(message = "制度类别不能为空")
    private String category;

    @NotBlank(message = "制度正文不能为空")
    private String content;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    /** 是否直接发布（否则保存为草稿） */
    private boolean publish = false;

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

    public boolean isPublish() {
        return publish;
    }

    public void setPublish(boolean publish) {
        this.publish = publish;
    }
}
