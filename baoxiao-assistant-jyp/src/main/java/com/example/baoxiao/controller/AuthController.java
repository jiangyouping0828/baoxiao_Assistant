package com.example.baoxiao.controller;

import com.example.baoxiao.domain.SysUser;
import com.example.baoxiao.dto.LoginRequest;
import com.example.baoxiao.repository.SysUserRepository;
import com.example.baoxiao.security.SessionKeys;
import com.example.baoxiao.security.SessionUser;
import com.example.baoxiao.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SysUserRepository userRepository;

    public AuthController(AuthService authService, SysUserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public SessionUser login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        SessionUser user = authService.login(request);
        session.setAttribute(SessionKeys.LOGIN_USER, user);
        return user;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        session.invalidate();
        Map<String, Object> body = new HashMap<>();
        body.put("message", "已退出登录");
        return body;
    }

    @GetMapping("/me")
    public SessionUser me(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER);
    }

    /** 演示账号列表，供登录页一键填充。 */
    @GetMapping("/demo-users")
    public List<Map<String, String>> demoUsers() {
        List<Map<String, String>> list = new ArrayList<>();
        for (SysUser user : userRepository.findAll()) {
            Map<String, String> item = new HashMap<>();
            item.put("username", user.getUsername());
            item.put("password", user.getPassword());
            item.put("displayName", user.getDisplayName());
            item.put("role", user.getRole().name());
            item.put("roleLabel", user.getRole().getLabel());
            item.put("department", user.getDepartment());
            list.add(item);
        }
        return list;
    }
}
