package com.example.baoxiao.controller;

import com.example.baoxiao.security.SessionKeys;
import com.example.baoxiao.security.SessionUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class PageController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        SessionUser user = (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER);
        model.addAttribute("user", user);
        return "index";
    }

    @GetMapping("/policies")
    public String policies(HttpSession session, Model model) {
        model.addAttribute("user", (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER));
        return "policies";
    }

    @GetMapping("/documents")
    public String documents(HttpSession session, Model model) {
        model.addAttribute("user", (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER));
        return "documents";
    }

    @GetMapping("/consult")
    public String consult(HttpSession session, Model model) {
        model.addAttribute("user", (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER));
        return "consult";
    }
}
