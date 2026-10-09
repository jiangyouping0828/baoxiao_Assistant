package com.example.baoxiao.security;

/**
 * 会话键常量与工具方法。
 */
public final class SessionKeys {

    public static final String LOGIN_USER = "LOGIN_USER";

    private SessionKeys() {
    }

    public static boolean isApi(javax.servlet.http.HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith("/api/") || (accept != null && accept.contains("application/json"));
    }
}
