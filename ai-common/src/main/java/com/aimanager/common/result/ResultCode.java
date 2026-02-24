package com.aimanager.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {
    
    // 成功
    SUCCESS(200, "操作成功"),
    
    // 失败
    FAIL(500, "操作失败"),
    
    // 参数错误
    PARAM_ERROR(400, "参数错误"),
    PARAM_MISSING(401, "缺少必要参数"),
    PARAM_INVALID(402, "参数格式不正确"),
    
    // 认证授权
    UNAUTHORIZED(1001, "未认证"),
    TOKEN_INVALID(1002, "Token无效"),
    TOKEN_EXPIRED(1003, "Token已过期"),
    FORBIDDEN(1004, "无权限访问"),
    
    // 用户相关
    USER_NOT_FOUND(2001, "用户不存在"),
    USER_NOT_EXIST(2001, "用户不存在"),  // 别名
    USER_ALREADY_EXISTS(2002, "用户已存在"),
    USER_DISABLED(2003, "用户已被禁用"),
    PASSWORD_ERROR(2004, "密码错误"),
    OPERATION_NOT_ALLOWED(2005, "操作不允许"),
    
    // 组织相关
    ORG_NOT_FOUND(3001, "组织不存在"),
    ORG_ALREADY_EXISTS(3002, "组织已存在"),
    
    // 角色权限相关
    ROLE_NOT_FOUND(4001, "角色不存在"),
    ROLE_ALREADY_EXISTS(4002, "角色已存在"),
    PERMISSION_DENIED(4003, "权限不足"),
    
    // AI模型相关
    MODEL_NOT_FOUND(5001, "模型不存在"),
    MODEL_CONFIG_ERROR(5002, "模型配置错误"),
    MODEL_CALL_ERROR(5003, "模型调用失败"),
    API_KEY_INVALID(5004, "API密钥无效"),
    
    // 知识库相关
    KNOWLEDGE_NOT_FOUND(6001, "知识不存在"),
    DOCUMENT_NOT_FOUND(6002, "文档不存在"),
    DOCUMENT_PARSE_ERROR(6003, "文档解析失败"),
    CATEGORY_NOT_FOUND(6004, "分类不存在"),
    
    // 向量相关
    VECTOR_GENERATE_ERROR(7001, "向量生成失败"),
    VECTOR_SEARCH_ERROR(7002, "向量检索失败"),
    
    // 文件相关
    FILE_UPLOAD_ERROR(8001, "文件上传失败"),
    FILE_SIZE_EXCEED(8002, "文件大小超出限制"),
    FILE_TYPE_ERROR(8003, "文件类型不支持"),
    
    // 系统错误
    SYSTEM_ERROR(9001, "系统错误"),
    DATABASE_ERROR(9002, "数据库错误"),
    NETWORK_ERROR(9003, "网络错误"),
    MODEL_ALREADY_EXISTS(5004, "模型已存在"),
    MODEL_NOT_EXIST(5005, "模型不存在");

    private final Integer code;
    private final String message;
    
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}

