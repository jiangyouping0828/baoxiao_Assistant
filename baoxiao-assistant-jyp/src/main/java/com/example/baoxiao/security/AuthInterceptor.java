package com.example.baoxiao.security;

import com.example.baoxiao.domain.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

/**
 * 登录与角色鉴权拦截器。
 *
 * <ul>
 *   <li>未登录：页面请求跳转 /login，接口请求返回 401 JSON；</li>
 *   <li>已登录但角色不满足 {@link RoleRequired}：返回 403。</li>
 * </ul>
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        SessionUser user = session == null ? null : (SessionUser) session.getAttribute(SessionKeys.LOGIN_USER);

        if (user == null) {
            if (SessionKeys.isApi(request)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"未登录或登录已过期\",\"code\":401}");
            } else {
                response.sendRedirect(request.getContextPath() + "/login");
            }
            return false;
        }

        RoleRequired required = resolveRoleRequired(handler);
        if (required != null && !hasRole(user, required.value())) {
            if (SessionKeys.isApi(request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"error\":\"无权访问：当前角色为" + user.getRoleLabel() + "\",\"code\":403}");
            } else {
                response.sendRedirect(request.getContextPath() + "/?forbidden=1");
            }
            return false;
        }
        return true;
    }

    private RoleRequired resolveRoleRequired(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return null;
        }
        HandlerMethod method = (HandlerMethod) handler;
        RoleRequired onMethod = method.getMethodAnnotation(RoleRequired.class);
        if (onMethod != null) {
            return onMethod;
        }
        return method.getBeanType().getAnnotation(RoleRequired.class);
    }

    private boolean hasRole(SessionUser user, UserRole[] roles) {
        return Arrays.stream(roles).anyMatch(role -> role == user.getRole());
    }
}
