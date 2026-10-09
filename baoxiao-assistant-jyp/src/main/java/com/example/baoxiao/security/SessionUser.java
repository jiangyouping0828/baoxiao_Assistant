package com.example.baoxiao.security;

import com.example.baoxiao.domain.SysUser;
import com.example.baoxiao.domain.UserRole;

import java.io.Serializable;

/**
 * 登录会话中保存的当前用户。不保存密码。
 */
public class SessionUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String username;
    private final String displayName;
    private final UserRole role;
    private final String department;

    public SessionUser(Long id, String username, String displayName, UserRole role, String department) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.role = role;
        this.department = department;
    }

    public static SessionUser of(SysUser user) {
        return new SessionUser(user.getId(), user.getUsername(), user.getDisplayName(),
                user.getRole(), user.getDepartment());
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isFinance() {
        return role == UserRole.FINANCE;
    }

    public boolean isEmployee() {
        return role == UserRole.EMPLOYEE;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public UserRole getRole() {
        return role;
    }

    public String getRoleLabel() {
        return role == null ? "" : role.getLabel();
    }

    public String getDepartment() {
        return department;
    }
}
