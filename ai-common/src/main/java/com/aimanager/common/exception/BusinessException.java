package com.aimanager.common.exception;

import com.aimanager.common.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private Integer code;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }
    
    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }
}

