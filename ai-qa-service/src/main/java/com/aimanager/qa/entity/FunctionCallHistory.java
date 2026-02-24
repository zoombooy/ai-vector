package com.aimanager.qa.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 函数调用历史实体
 */
@Data
@TableName("t_function_call_history")
public class FunctionCallHistory {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 函数ID
     */
    private Long functionId;
    
    /**
     * 函数名称
     */
    private String functionName;
    
    /**
     * 输入参数（JSON格式）
     */
    private String inputParams;
    
    /**
     * 输出结果（JSON格式）
     */
    private String outputResult;
    
    /**
     * 调用状态（SUCCESS/FAILED/TIMEOUT）
     */
    private String status;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

