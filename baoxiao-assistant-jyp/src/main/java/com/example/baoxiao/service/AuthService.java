package com.example.baoxiao.service;

import com.example.baoxiao.domain.SysUser;
import com.example.baoxiao.dto.LoginRequest;
import com.example.baoxiao.repository.SysUserRepository;
import com.example.baoxiao.security.SessionUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 登录认证服务。
 *
 * <p>演示项目采用明文口令比对 + Session 会话。生产环境应替换为
 * BCrypt 加盐哈希 + 统一身份认证（SSO / OAuth2）。</p>
 */
@Service
public class AuthService {

    private final SysUserRepository userRepository;

    public AuthService(SysUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public SessionUser login(LoginRequest request) {
        Optional<SysUser> found = userRepository.findByUsername(request.getUsername().trim());
        if (!found.isPresent()) {
            throw new BizException(401, "用户名或密码错误");
        }
        SysUser user = found.get();
        if (!user.getPassword().equals(request.getPassword())) {
            throw new BizException(401, "用户名或密码错误");
        }
        return SessionUser.of(user);
    }
}
