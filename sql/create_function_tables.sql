-- ========================================
-- Function Call 功能相关表
-- ========================================

-- 1. 外部函数定义表
CREATE TABLE IF NOT EXISTS `t_external_function` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `function_name` VARCHAR(100) NOT NULL COMMENT '函数名称（唯一标识）',
    `display_name` VARCHAR(200) NOT NULL COMMENT '显示名称',
    `description` TEXT COMMENT '函数描述（告诉AI这个函数的用途）',
    `api_url` VARCHAR(500) NOT NULL COMMENT 'API接口地址',
    `http_method` VARCHAR(10) DEFAULT 'POST' COMMENT 'HTTP方法（GET/POST/PUT/DELETE）',
    `headers` TEXT COMMENT '请求头（JSON格式）',
    `auth_type` VARCHAR(50) COMMENT '认证类型（NONE/BEARER/API_KEY/BASIC）',
    `auth_config` TEXT COMMENT '认证配置（JSON格式）',
    `parameters_schema` TEXT COMMENT '参数Schema（JSON Schema格式，定义函数参数）',
    `response_mapping` TEXT COMMENT '响应映射配置（JSON格式，如何提取响应中的有用信息）',
    `timeout` INT DEFAULT 30 COMMENT '超时时间（秒）',
    `retry_times` INT DEFAULT 0 COMMENT '重试次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用 1-启用）',
    `category` VARCHAR(50) COMMENT '分类（天气/股票/新闻等）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(64) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(64) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除 1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_function_name` (`function_name`),
    KEY `idx_status` (`status`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部函数定义表';

-- 2. 函数调用历史表
CREATE TABLE IF NOT EXISTS `t_function_call_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `function_id` BIGINT NOT NULL COMMENT '函数ID',
    `function_name` VARCHAR(100) NOT NULL COMMENT '函数名称',
    `input_params` TEXT COMMENT '输入参数（JSON格式）',
    `output_result` TEXT COMMENT '输出结果（JSON格式）',
    `status` VARCHAR(20) COMMENT '调用状态（SUCCESS/FAILED/TIMEOUT）',
    `error_message` TEXT COMMENT '错误信息',
    `response_time` BIGINT COMMENT '响应时间（毫秒）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_function_id` (`function_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='函数调用历史表';

-- 3. 插入示例函数：天气查询
INSERT INTO `t_external_function` (
    `function_name`,
    `display_name`,
    `description`,
    `api_url`,
    `http_method`,
    `headers`,
    `auth_type`,
    `parameters_schema`,
    `response_mapping`,
    `timeout`,
    `status`,
    `category`
) VALUES (
    'get_weather',
    '获取天气信息',
    '获取指定城市的实时天气信息，包括温度、湿度、天气状况等。当用户询问天气相关问题时调用此函数。',
    'https://api.seniverse.com/v3/weather/now.json',
    'GET',
    '{"Content-Type": "application/json"}',
    'API_KEY',
    '{
        "type": "object",
        "properties": {
            "location": {
                "type": "string",
                "description": "城市名称，例如：北京、上海、深圳"
            }
        },
        "required": ["location"]
    }',
    '{
        "temperature": "$.results[0].now.temperature",
        "text": "$.results[0].now.text",
        "feels_like": "$.results[0].now.feels_like"
    }',
    10,
    0,
    '天气'
);

-- 4. 插入示例函数：时间查询
INSERT INTO `t_external_function` (
    `function_name`,
    `display_name`,
    `description`,
    `api_url`,
    `http_method`,
    `headers`,
    `auth_type`,
    `parameters_schema`,
    `response_mapping`,
    `timeout`,
    `status`,
    `category`
) VALUES (
    'get_current_time',
    '获取当前时间',
    '获取当前的日期和时间。当用户询问现在几点、今天日期等问题时调用此函数。',
    'http://worldtimeapi.org/api/timezone/Asia/Shanghai',
    'GET',
    '{"Content-Type": "application/json"}',
    'NONE',
    '{
        "type": "object",
        "properties": {}
    }',
    '{
        "datetime": "$.datetime",
        "timezone": "$.timezone"
    }',
    10,
    1,
    '时间'
);

-- 5. 插入示例函数：计算器
INSERT INTO `t_external_function` (
    `function_name`,
    `display_name`,
    `description`,
    `api_url`,
    `http_method`,
    `headers`,
    `auth_type`,
    `parameters_schema`,
    `response_mapping`,
    `timeout`,
    `status`,
    `category`
) VALUES (
    'calculate',
    '数学计算',
    '执行数学计算。当用户需要进行复杂的数学运算时调用此函数。',
    'http://api.mathjs.org/v4/',
    'POST',
    '{"Content-Type": "application/json"}',
    'NONE',
    '{
        "type": "object",
        "properties": {
            "expr": {
                "type": "string",
                "description": "数学表达式，例如：2 + 2、sqrt(16)、sin(pi/2)"
            }
        },
        "required": ["expr"]
    }',
    '{
        "result": "$"
    }',
    10,
    1,
    '计算'
);

