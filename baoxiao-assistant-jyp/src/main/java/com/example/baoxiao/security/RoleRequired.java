package com.example.baoxiao.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.example.baoxiao.domain.UserRole;

/**
 * 标注接口/页面所需角色。未标注表示登录即可访问。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RoleRequired {

    UserRole[] value();
}
