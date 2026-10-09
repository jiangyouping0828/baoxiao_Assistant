package com.example.baoxiao.controller;

import com.example.baoxiao.domain.Policy;
import com.example.baoxiao.domain.Rule;
import com.example.baoxiao.domain.UserRole;
import com.example.baoxiao.dto.PolicyForm;
import com.example.baoxiao.security.RoleRequired;
import com.example.baoxiao.security.SessionKeys;
import com.example.baoxiao.security.SessionUser;
import com.example.baoxiao.service.PolicyService;
import com.example.baoxiao.service.RuleService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PolicyController {

    private final PolicyService policyService;
    private final RuleService ruleService;

    public PolicyController(PolicyService policyService, RuleService ruleService) {
        this.policyService = policyService;
        this.ruleService = ruleService;
    }

    // ---------------- 员工：自助查询有效制度 ----------------

    /** 所有登录用户可见的「现行有效制度」。 */
    @GetMapping("/policies/valid")
    public List<Policy> validPolicies() {
        return policyService.listValid();
    }

    // ---------------- 行政专员（乔姐）：制度维护 ----------------

    /** 全量制度（含草稿/归档），仅行政专员可见。 */
    @GetMapping("/policies")
    @RoleRequired(UserRole.ADMIN)
    public List<Policy> allPolicies() {
        return policyService.listAll();
    }

    @PostMapping("/policies")
    @RoleRequired(UserRole.ADMIN)
    public Policy savePolicy(@Valid @RequestBody PolicyForm form, HttpSession session) {
        return policyService.createOrUpdate(form, currentUser(session).getDisplayName());
    }

    @PostMapping("/policies/{id}/publish")
    @RoleRequired(UserRole.ADMIN)
    public Policy publish(@PathVariable Long id, HttpSession session) {
        return policyService.publish(id, currentUser(session).getDisplayName());
    }

    @PostMapping("/policies/{id}/archive")
    @RoleRequired(UserRole.ADMIN)
    public Policy archive(@PathVariable Long id, HttpSession session) {
        return policyService.archive(id, currentUser(session).getDisplayName());
    }

    @DeleteMapping("/policies/{id}")
    @RoleRequired(UserRole.ADMIN)
    public Map<String, Object> delete(@PathVariable Long id) {
        policyService.delete(id);
        Map<String, Object> body = new HashMap<>();
        body.put("message", "制度已删除");
        return body;
    }

    // ---------------- 行政专员：额度规则维护 ----------------

    @GetMapping("/rules")
    public List<Rule> rules() {
        return ruleService.listRules();
    }

    @PostMapping("/rules/parse")
    @RoleRequired(UserRole.ADMIN)
    public List<Rule> parseRules(@RequestBody Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        if (text.trim().isEmpty()) {
            throw com.example.baoxiao.service.BizException.badRequest("规则文本不能为空");
        }
        return ruleService.parseAndReplace(text);
    }

    private SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER);
    }
}
