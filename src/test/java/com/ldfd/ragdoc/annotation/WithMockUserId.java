package com.ldfd.ragdoc.annotation;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

/**
 * 带有 userId 的模拟用户注解
 *
 * 用于测试中创建具有特定 id 的模拟用户，该 id 会被包含在 JWT token 中
 *
 * 使用示例：
 * <pre>
 * @Test
 * @WithMockUserId(username = "testuser", id = 1L)
 * void testSomeMethod() {
 *     // 测试代码
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserIdSecurityContextFactory.class)
public @interface WithMockUserId {

    /**
     * 用户名
     */
    String username() default "user";

    /**
     * 用户 ID（会被存放到 JWT 中）
     */
    long id() default 1L;

    /**
     * 权限列表
     */
    String[] roles() default {"ROLE_USER"};

    /**
     * 用户邮箱
     */
    String email() default "test@example.com";

    /**
     * 用户全名
     */
    String fullName() default "Test User";
}
