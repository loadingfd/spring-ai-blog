package com.ldfd.ragdoc.exception;

import java.io.Serial;

/**
 * 业务异常
 * 用于处理业务规则违反等异常情况
 */
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private String code;

    public BusinessException(String message) {
        super(message);
        this.code = "400";
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "400";
    }

    public BusinessException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
