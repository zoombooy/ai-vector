package com.aimanager.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一响应结果
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * 成功返回
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }
    
    /**
     * 成功返回（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }
    
    /**
     * 成功返回（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }
    
    /**
     * 失败返回
     */
    public static <T> Result<T> fail() {
        return new Result<>(ResultCode.FAIL.getCode(), ResultCode.FAIL.getMessage(), null);
    }
    
    /**
     * 失败返回（自定义消息）
     */
    public static <T> Result<T> fail(String message) {
        return new Result<>(ResultCode.FAIL.getCode(), message, null);
    }
    
    /**
     * 失败返回（自定义状态码和消息）
     */
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 失败返回（使用ResultCode）
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}

