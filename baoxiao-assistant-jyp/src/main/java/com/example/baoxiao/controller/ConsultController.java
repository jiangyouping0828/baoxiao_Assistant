package com.example.baoxiao.controller;

import com.example.baoxiao.dto.ConsultRequest;
import com.example.baoxiao.dto.ConsultResponse;
import com.example.baoxiao.service.ConsultationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class ConsultController {

    private final ConsultationService consultationService;

    public ConsultController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    /** 员工自助查询有效制度。 */
    @PostMapping("/consult")
    public ConsultResponse consult(@Valid @RequestBody ConsultRequest request) {
        return consultationService.consult(request.getQuestion());
    }
}
