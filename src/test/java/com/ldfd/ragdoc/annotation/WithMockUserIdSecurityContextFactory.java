package com.ldfd.ragdoc.annotation;

import com.ldfd.ragdoc.config.auth.CustomUserDetails;
import com.ldfd.ragdoc.domain.bo.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

/**
 * WithMockUserId 注解的安全上下文工厂
 *
 * 用于在测试中创建包含 userId 的 Authentication 对象
 */
public class WithMockUserIdSecurityContextFactory implements WithSecurityContextFactory<WithMockUserId> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserId annotation) {
        // 创建用户对象
        User user = new User();
        user.setId(annotation.id());
        user.setUsername(annotation.username());
        user.setEmail(annotation.email());
        user.setFullName(annotation.fullName());
        user.setPassword("password"); // 默认密码
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());

        // 创建 CustomUserDetails
        CustomUserDetails userDetails = new CustomUserDetails(user);

        // 创建权限列表
        List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.roles())
                .map(SimpleGrantedAuthority::new)
                .toList();

        // 创建 Authentication
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                userDetails,
                userDetails.getPassword(),
                authorities
        );

        // 创建并设置 SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        return context;
    }
}
