package com.aimanager.qa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatMessage {

    /**
     * 角色：system、user、assistant、function
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 函数调用信息（当 role=assistant 且模型决定调用函数时）
     */
    private ModelChatResponse.FunctionCall functionCall;

    /**
     * 函数名称（当 role=function 时）
     */
    private String name;

    /**
     * 便捷构造函数（只设置 role 和 content）
     */
    public ChatMessage(String role, String content) {
        this.role = role;
        this.content = content;
    }
}

