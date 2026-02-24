package com.aimanager.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    
    /**
     * 角色：system、user、assistant
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
}

