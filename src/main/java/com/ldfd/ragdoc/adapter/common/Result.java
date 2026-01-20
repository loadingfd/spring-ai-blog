package com.ldfd.ragdoc.adapter.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * 统一响应结果类
 * 所有API接口都返回此格式的数据
 *
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 状态码：0 表示成功，非0 表示失败
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功响应
     */
    public Result(T data) {
        this.data = data;
        this.code = 200;
        this.message = "Success";
    }

    public Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }


}