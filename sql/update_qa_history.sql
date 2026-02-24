-- 更新问答历史表结构
USE ai_knowledge_platform;

-- 删除旧表
DROP TABLE IF EXISTS t_qa_history;

-- 重新创建问答历史表
CREATE TABLE t_qa_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    user_id BIGINT COMMENT '用户ID',
    question TEXT NOT NULL COMMENT '问题',
    answer TEXT COMMENT '答案',
    model_id BIGINT COMMENT '使用的模型ID',
    model_name VARCHAR(100) COMMENT '模型名称',
    response_time BIGINT COMMENT '响应时间（毫秒）',
    total_tokens INT COMMENT '使用的tokens数',
    related_docs VARCHAR(500) COMMENT '相关文档ID（逗号分隔）',
    feedback TINYINT COMMENT '反馈（1-有帮助，0-无帮助）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='问答历史表';

