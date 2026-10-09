package com.example.baoxiao.controller;

import com.example.baoxiao.domain.ExpenseDoc;
import com.example.baoxiao.domain.UserRole;
import com.example.baoxiao.dto.DocumentForm;
import com.example.baoxiao.dto.VerifyRequest;
import com.example.baoxiao.security.RoleRequired;
import com.example.baoxiao.security.SessionKeys;
import com.example.baoxiao.security.SessionUser;
import com.example.baoxiao.service.DocumentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // ---------------- 员工：经授权查询本人单据 ----------------

    @GetMapping("/my")
    public List<ExpenseDoc> myDocuments(HttpSession session) {
        return documentService.listOwnedBy(currentUser(session).getUsername());
    }

    @GetMapping("/my/{id}")
    public ExpenseDoc myDocument(@PathVariable Long id, HttpSession session) {
        // 跨人访问会抛出 403，落实「仅本人」
        return documentService.getOwnedDoc(id, currentUser(session).getUsername());
    }

    @PostMapping
    public ExpenseDoc create(@Valid @RequestBody DocumentForm form, HttpSession session) {
        return documentService.create(currentUser(session), form);
    }

    // ---------------- 财务专员（周敏）：核对业务事实 ----------------

    @GetMapping
    @RoleRequired(UserRole.FINANCE)
    public List<ExpenseDoc> allDocuments() {
        return documentService.listAll();
    }

    @PostMapping("/{id}/checking")
    @RoleRequired(UserRole.FINANCE)
    public ExpenseDoc startChecking(@PathVariable Long id, HttpSession session) {
        return documentService.startChecking(id, currentUser(session).getDisplayName());
    }

    @PostMapping("/{id}/verify")
    @RoleRequired(UserRole.FINANCE)
    public ExpenseDoc verify(@PathVariable Long id, @RequestBody VerifyRequest request, HttpSession session) {
        return documentService.verify(id, currentUser(session).getDisplayName(),
                request.isVerified(), request.getNote());
    }

    private SessionUser currentUser(HttpSession session) {
        return (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER);
    }
}
