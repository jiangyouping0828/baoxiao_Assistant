package com.example.baoxiao.dto;

import javax.validation.constraints.NotBlank;

public class ConsultRequest {

    @NotBlank(message = "问题不能为空")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
