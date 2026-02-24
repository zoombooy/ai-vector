-- ========================================
-- Agent 接入功能相关表
-- ========================================

-- 1. 外部Agent注册表
CREATE TABLE IF NOT EXISTS `t_external_agent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_name` VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    `agent_code` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Agent唯一编码',
    `agent_type` VARCHAR(50) DEFAULT 'PYTHON' COMMENT 'Agent类型（PYTHON/JAVA/HTTP）',
    `description` TEXT COMMENT 'Agent描述（告诉AI这个Agent的用途和能力）',
    `endpoint_url` VARCHAR(500) COMMENT 'Agent服务地址（HTTP类型Agent使用）',
    `endpoint_type` VARCHAR(20) DEFAULT 'HTTP' COMMENT '通信方式（HTTP/GRPC/WEBSOCKET）',
    `auth_type` VARCHAR(50) DEFAULT 'NONE' COMMENT '认证类型（NONE/BEARER/API_KEY）',
    `auth_config` TEXT COMMENT '认证配置（JSON格式）',
    `input_schema` TEXT COMMENT '输入参数Schema（JSON Schema格式）',
    `output_schema` TEXT COMMENT '输出结果Schema（JSON Schema格式）',
    `capabilities` TEXT COMMENT 'Agent能力列表（JSON数组）',
    `timeout` INT DEFAULT 30 COMMENT '超时时间（秒）',
    `retry_times` INT DEFAULT 0 COMMENT '重试次数',
    `max_concurrent` INT DEFAULT 10 COMMENT '最大并发数',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `version` VARCHAR(20) DEFAULT '1.0.0' COMMENT 'Agent版本',
    `category` VARCHAR(50) COMMENT '分类（数据分析/文本处理/图像处理等）',
    `tags` VARCHAR(500) COMMENT '标签（逗号分隔）',
    `priority` INT DEFAULT 0 COMMENT '优先级（数字越大优先级越高）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_code` (`agent_code`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`),
    KEY `idx_agent_type` (`agent_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部Agent注册表';

-- 2. Agent执行历史表
CREATE TABLE IF NOT EXISTS `t_agent_execution_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `agent_id` BIGINT NOT NULL COMMENT 'Agent ID',
    `agent_code` VARCHAR(50) NOT NULL COMMENT 'Agent编码',
    `input_data` TEXT COMMENT '输入数据（JSON格式）',
    `output_data` TEXT COMMENT '输出数据（JSON格式）',
    `status` VARCHAR(20) COMMENT '执行状态（SUCCESS/FAILED/TIMEOUT/RUNNING）',
    `error_message` TEXT COMMENT '错误信息',
    `start_time` DATETIME COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `execution_time` BIGINT COMMENT '执行时间（毫秒）',
    `user_id` BIGINT COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_agent_id` (`agent_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent执行历史表';

-- 3. Agent配置参数表
CREATE TABLE IF NOT EXISTS `t_agent_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `agent_id` BIGINT NOT NULL COMMENT 'Agent ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(20) DEFAULT 'STRING' COMMENT '配置类型（STRING/NUMBER/BOOLEAN/JSON）',
    `description` VARCHAR(500) COMMENT '配置描述',
    `is_secret` TINYINT DEFAULT 0 COMMENT '是否敏感信息（0-否 1-是）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_config` (`agent_id`, `config_key`),
    KEY `idx_agent_id` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent配置参数表';

-- 4. 插入示例Agent：天气查询Agent
INSERT INTO `t_external_agent` (
    `agent_name`,
    `agent_code`,
    `agent_type`,
    `description`,
    `endpoint_url`,
    `input_schema`,
    `output_schema`,
    `capabilities`,
    `timeout`,
    `status`,
    `category`,
    `tags`
) VALUES (
    '天气查询Agent',
    'weather_agent',
    'PYTHON',
    '查询指定城市的天气信息，包括温度、湿度、天气状况等。当用户询问天气相关问题时可以调用此Agent。',
    'http://localhost:5001/execute',
    '{
        "type": "object",
        "properties": {
            "city": {
                "type": "string",
                "description": "城市名称，例如：北京、上海、深圳"
            }
        },
        "required": ["city"]
    }',
    '{
        "type": "object",
        "properties": {
            "temperature": {"type": "string", "description": "温度"},
            "weather": {"type": "string", "description": "天气状况"},
            "humidity": {"type": "string", "description": "湿度"}
        }
    }',
    '["天气查询", "实时天气", "天气预报"]',
    10,
    1,
    '天气服务',
    '天气,查询,实时'
);

-- 5. 插入示例Agent：数据分析Agent
INSERT INTO `t_external_agent` (
    `agent_name`,
    `agent_code`,
    `agent_type`,
    `description`,
    `endpoint_url`,
    `input_schema`,
    `output_schema`,
    `capabilities`,
    `timeout`,
    `status`,
    `category`,
    `tags`
) VALUES (
    '数据分析Agent',
    'data_analysis_agent',
    'PYTHON',
    '对数据进行统计分析，支持求和、平均值、最大值、最小值等基本统计功能。',
    'http://localhost:5002/execute',
    '{
        "type": "object",
        "properties": {
            "data": {
                "type": "array",
                "items": {"type": "number"},
                "description": "数字数组"
            },
            "operation": {
                "type": "string",
                "enum": ["sum", "avg", "max", "min"],
                "description": "操作类型"
            }
        },
        "required": ["data", "operation"]
    }',
    '{
        "type": "object",
        "properties": {
            "result": {"type": "number", "description": "计算结果"}
        }
    }',
    '["数据统计", "数据分析", "数学计算"]',
    30,
    1,
    '数据分析',
    '数据,分析,统计'
);

