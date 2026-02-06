package com.ldfd.ragdoc.exception.handler;

import com.ldfd.ragdoc.adapter.common.Result;
import com.ldfd.ragdoc.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 * 捕获应用中的异常并返回统一的错误响应格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 BusinessException
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result> handleBusinessException(BusinessException e) {
        log.warn("Business exception: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(Integer.parseInt(e.getCode()), e.getMessage()));
    }

    /**
     * 处理 AuthorizationDeniedException (Spring Security 6.x)
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Result> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.error("Authorization denied - Message: {}, Cause: {}",
                e.getMessage(),
                e.getCause() != null ? e.getCause().getMessage() : "None",
                e);

        String detailedMessage = "访问被拒绝。可能原因：" +
                "1) JWT token 无效或已过期，" +
                "2) 用户未登录或认证失败，" +
                "3) 权限不足。请检查您的认证状态并重新登录。";

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(HttpStatus.FORBIDDEN.value(), detailedMessage));
    }

    /**
     * 处理 AccessDeniedException (传统的访问拒绝异常)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result> handleAccessDeniedException(AccessDeniedException e) {
        log.error("Access denied - Message: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Result.fail(HttpStatus.FORBIDDEN.value(), "访问被拒绝，您没有足够的权限访问此资源"));
    }


    /**
     * 处理 AuthenticationException
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Result> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Result.fail(HttpStatus.UNAUTHORIZED.value(), "认证失败，请重新登录"));
    }

    /**
     * 处理 ResponseStatusException
     * 用于流式响应前的认证/授权检查
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Result> handleResponseStatusException(ResponseStatusException e) {
        log.warn("Response status exception: {}", e.getReason());
        return ResponseEntity.status(e.getStatusCode())
                .body(Result.fail(e.getStatusCode().value(), e.getReason() != null ? e.getReason() : "请求失败"));
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result> handleException(Exception e) {
        log.error("System exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(500, "System error: " + e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        return Result.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .findFirst()
                .orElse("Invalid request");
        return Result.fail(HttpStatus.BAD_REQUEST.value(), message);
    }
}
