-- MCP Server配置表
CREATE TABLE IF NOT EXISTS `t_mcp_server` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `server_name` VARCHAR(100) NOT NULL COMMENT 'MCP Server名称',
    `server_code` VARCHAR(100) NOT NULL UNIQUE COMMENT 'MCP Server唯一编码',
    `description` TEXT COMMENT '描述',
    `mcp_url` VARCHAR(500) NOT NULL COMMENT 'MCP Server URL（HTTP Stream地址）',
    `server_type` VARCHAR(50) DEFAULT 'HTTP_STREAM' COMMENT '服务器类型（HTTP_STREAM/STDIO/SSE）',
    `env_config` TEXT COMMENT '环境变量配置（JSON格式）',
    `auth_type` VARCHAR(50) DEFAULT 'NONE' COMMENT '认证类型（NONE/BEARER/API_KEY）',
    `auth_config` TEXT COMMENT '认证配置（JSON格式）',
    `source` VARCHAR(50) DEFAULT 'MANUAL' COMMENT '来源（MANUAL/MCPMARKET）',
    `source_id` VARCHAR(100) COMMENT '来源平台的ID',
    `source_url` VARCHAR(500) COMMENT '来源平台的链接',
    `logo_url` VARCHAR(500) COMMENT 'Logo图片URL',
    `category` VARCHAR(50) COMMENT '分类',
    `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
    `priority` INT DEFAULT 0 COMMENT '优先级',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `timeout` INT DEFAULT 30000 COMMENT '超时时间（毫秒）',
    `last_health_check` DATETIME COMMENT '最后健康检查时间',
    `health_status` VARCHAR(20) DEFAULT 'UNKNOWN' COMMENT '健康状态（HEALTHY/UNHEALTHY/UNKNOWN）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_server_code` (`server_code`),
    INDEX `idx_status` (`status`),
    INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP Server配置表';

-- MCP工具表（存储MCP Server提供的具体工具）
CREATE TABLE IF NOT EXISTS `t_mcp_tool` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `server_id` BIGINT NOT NULL COMMENT '所属MCP Server ID',
    `tool_name` VARCHAR(100) NOT NULL COMMENT '工具名称',
    `tool_code` VARCHAR(100) NOT NULL COMMENT '工具唯一编码（在Server范围内唯一）',
    `description` TEXT COMMENT '工具描述',
    `input_schema` TEXT COMMENT '输入参数Schema（JSON Schema格式）',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用（0-禁用 1-启用）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '是否删除',
    INDEX `idx_server_id` (`server_id`),
    INDEX `idx_tool_code` (`tool_code`),
    UNIQUE KEY `uk_server_tool` (`server_id`, `tool_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP工具表';

-- MCP调用历史表
CREATE TABLE IF NOT EXISTS `t_mcp_call_history` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `session_id` VARCHAR(64) COMMENT '会话ID',
    `server_id` BIGINT COMMENT 'MCP Server ID',
    `server_code` VARCHAR(100) COMMENT 'MCP Server编码',
    `tool_name` VARCHAR(100) COMMENT '工具名称',
    `user_id` BIGINT COMMENT '用户ID',
    `input_data` TEXT COMMENT '输入数据',
    `output_data` TEXT COMMENT '输出数据',
    `status` VARCHAR(20) COMMENT '执行状态（RUNNING/SUCCESS/FAILED）',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `execution_time` BIGINT COMMENT '执行时间（毫秒）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_server_id` (`server_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP调用历史表';

