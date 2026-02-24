-- AI知识库管理平台数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS ai_knowledge_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ai_knowledge_platform;

-- ====================================
-- 用户与权限相关表
-- ====================================

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 组织表
CREATE TABLE IF NOT EXISTS t_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '组织ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父组织ID',
    org_name VARCHAR(100) NOT NULL COMMENT '组织名称',
    org_code VARCHAR(50) COMMENT '组织编码',
    org_type TINYINT COMMENT '组织类型（1-公司，2-部门，3-小组）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent_id (parent_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织表';

-- 角色表
CREATE TABLE IF NOT EXISTS t_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_type TINYINT DEFAULT 2 COMMENT '角色类型（1-系统角色，2-自定义角色）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_role_code (role_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE IF NOT EXISTS t_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_type TINYINT COMMENT '权限类型（1-菜单，2-按钮，3-接口）',
    path VARCHAR(255) COMMENT '路径',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent_id (parent_id),
    INDEX idx_permission_code (permission_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS t_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS t_role_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 用户组织关联表
CREATE TABLE IF NOT EXISTS t_user_organization (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    org_id BIGINT NOT NULL COMMENT '组织ID',
    is_primary TINYINT DEFAULT 0 COMMENT '是否主组织（0-否，1-是）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    INDEX idx_user_id (user_id),
    INDEX idx_org_id (org_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户组织关联表';

-- ====================================
-- AI模型管理相关表
-- ====================================

-- AI模型表
CREATE TABLE IF NOT EXISTS t_ai_model (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
    model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
    model_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模型编码',
    model_type VARCHAR(50) COMMENT '模型类型（openai、baidu、xunfei等）',
    model_version VARCHAR(50) COMMENT '模型版本',
    provider VARCHAR(100) COMMENT '提供商',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_model_code (model_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型表';

-- 模型配置表
CREATE TABLE IF NOT EXISTS t_model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    api_url VARCHAR(255) COMMENT 'API地址',
    api_key VARCHAR(255) COMMENT 'API密钥（加密）',
    max_tokens INT COMMENT '最大Token数',
    temperature DECIMAL(3,2) COMMENT '温度参数',
    top_p DECIMAL(3,2) COMMENT 'Top P参数',
    frequency_penalty DECIMAL(3,2) COMMENT '频率惩罚',
    presence_penalty DECIMAL(3,2) COMMENT '存在惩罚',
    timeout INT DEFAULT 30 COMMENT '超时时间（秒）',
    retry_times INT DEFAULT 3 COMMENT '重试次数',
    config_json TEXT COMMENT '其他配置（JSON格式）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_model_id (model_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置表';

-- 模型调用日志表
CREATE TABLE IF NOT EXISTS t_model_invocation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    model_id BIGINT NOT NULL COMMENT '模型ID',
    user_id BIGINT COMMENT '调用用户ID',
    request_content TEXT COMMENT '请求内容',
    response_content TEXT COMMENT '响应内容',
    tokens_used INT COMMENT '使用Token数',
    cost DECIMAL(10,4) COMMENT '费用',
    duration INT COMMENT '耗时（毫秒）',
    status TINYINT COMMENT '状态（1-成功，0-失败）',
    error_message TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_model_id (model_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型调用日志表';

-- ====================================
-- 知识库相关表
-- ====================================

-- 知识分类表
CREATE TABLE IF NOT EXISTS t_knowledge_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) COMMENT '分类编码',
    category_type TINYINT COMMENT '分类类型（1-数据库脚本，2-业务知识，3-技术文档，4-其他）',
    icon VARCHAR(100) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_parent_id (parent_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识分类表';

-- 文档表
CREATE TABLE IF NOT EXISTS t_document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '文档ID',
    category_id BIGINT COMMENT '分类ID',
    doc_title VARCHAR(200) NOT NULL COMMENT '文档标题',
    doc_type VARCHAR(50) COMMENT '文档类型（pdf、doc、txt、md等）',
    file_path VARCHAR(500) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    file_hash VARCHAR(64) COMMENT '文件哈希值',
    content LONGTEXT COMMENT '文档内容（文本）',
    summary TEXT COMMENT '摘要',
    tags VARCHAR(500) COMMENT '标签（逗号分隔）',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    status TINYINT DEFAULT 1 COMMENT '状态（0-草稿，1-已发布，2-已归档）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted),
    FULLTEXT idx_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档表';

-- 文档版本表
CREATE TABLE IF NOT EXISTS t_document_version (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '版本ID',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    version_number VARCHAR(20) NOT NULL COMMENT '版本号',
    file_path VARCHAR(500) COMMENT '文件路径',
    content LONGTEXT COMMENT '文档内容',
    change_log TEXT COMMENT '变更日志',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    INDEX idx_doc_id (doc_id),
    INDEX idx_version_number (version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档版本表';

-- 向量索引表
CREATE TABLE IF NOT EXISTS t_vector_index (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '索引ID',
    doc_id BIGINT NOT NULL COMMENT '文档ID',
    chunk_index INT COMMENT '分块索引',
    chunk_content TEXT COMMENT '分块内容',
    vector_id VARCHAR(100) COMMENT '向量数据库中的ID',
    embedding_model VARCHAR(100) COMMENT '向量化模型',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_doc_id (doc_id),
    INDEX idx_vector_id (vector_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='向量索引表';

-- 检索历史表
CREATE TABLE IF NOT EXISTS t_search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史ID',
    user_id BIGINT COMMENT '用户ID',
    search_keyword VARCHAR(500) COMMENT '检索关键词',
    search_type TINYINT COMMENT '检索类型（1-关键词，2-语义）',
    result_count INT COMMENT '结果数量',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检索历史表';

-- 问答历史表
CREATE TABLE IF NOT EXISTS t_qa_history (
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

-- ====================================
-- 应用集成相关表
-- ====================================

-- 外部工具表
CREATE TABLE IF NOT EXISTS t_external_tool (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '工具ID',
    tool_name VARCHAR(100) NOT NULL COMMENT '工具名称',
    tool_code VARCHAR(50) NOT NULL UNIQUE COMMENT '工具编码',
    tool_type VARCHAR(50) COMMENT '工具类型',
    api_url VARCHAR(255) COMMENT 'API地址',
    auth_type VARCHAR(50) COMMENT '认证类型（none、apikey、oauth2）',
    auth_config TEXT COMMENT '认证配置（JSON格式）',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_tool_code (tool_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外部工具表';

-- Agent表
CREATE TABLE IF NOT EXISTS t_agent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Agent ID',
    agent_name VARCHAR(100) NOT NULL COMMENT 'Agent名称',
    agent_code VARCHAR(50) NOT NULL UNIQUE COMMENT 'Agent编码',
    agent_type VARCHAR(50) COMMENT 'Agent类型',
    model_id BIGINT COMMENT '使用的模型ID',
    prompt_template TEXT COMMENT '提示词模板',
    capabilities TEXT COMMENT '能力配置（JSON格式）',
    trigger_condition TEXT COMMENT '触发条件',
    status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_agent_code (agent_code),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent表';

-- 接口配置表
CREATE TABLE IF NOT EXISTS t_interface_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '接口ID',
    interface_name VARCHAR(100) NOT NULL COMMENT '接口名称',
    interface_path VARCHAR(255) NOT NULL COMMENT '接口路径',
    method VARCHAR(10) COMMENT '请求方法（GET、POST等）',
    auth_required TINYINT DEFAULT 1 COMMENT '是否需要认证（0-否，1-是）',
    rate_limit INT COMMENT '频率限制（次/分钟）',
    description VARCHAR(500) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_interface_path (interface_path),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='接口配置表';

-- 集成调用日志表
CREATE TABLE IF NOT EXISTS t_integration_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_type TINYINT COMMENT '日志类型（1-工具调用，2-Agent执行，3-接口调用）',
    target_id BIGINT COMMENT '目标ID（工具ID、Agent ID等）',
    user_id BIGINT COMMENT '用户ID',
    request_data TEXT COMMENT '请求数据',
    response_data TEXT COMMENT '响应数据',
    status TINYINT COMMENT '状态（1-成功，0-失败）',
    error_message TEXT COMMENT '错误信息',
    duration INT COMMENT '耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_log_type (log_type),
    INDEX idx_target_id (target_id),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='集成调用日志表';

-- ====================================
-- 初始化数据
-- ====================================

-- 插入默认管理员用户（密码：admin123，需要加密）
INSERT INTO t_user (username, password, real_name, email, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 'admin@aimanager.com', 1);

-- 插入默认组织
INSERT INTO t_organization (parent_id, org_name, org_code, org_type, description)
VALUES (0, 'AI知识库平台', 'ROOT', 1, '根组织');

-- 插入默认角色
INSERT INTO t_role (role_name, role_code, role_type, description)
VALUES
('系统管理员', 'ROLE_ADMIN', 1, '系统管理员，拥有所有权限'),
('模型管理员', 'ROLE_MODEL_ADMIN', 1, '模型管理员，负责AI模型配置与维护'),
('知识管理员', 'ROLE_KNOWLEDGE_ADMIN', 1, '知识管理员，负责知识库维护'),
('普通用户', 'ROLE_USER', 1, '普通用户，使用平台查询知识');

-- 插入默认权限
INSERT INTO t_permission (parent_id, permission_name, permission_code, permission_type, path, sort_order)
VALUES
(0, '系统管理', 'system', 1, '/system', 1),
(0, '用户管理', 'user', 1, '/user', 2),
(0, '模型管理', 'model', 1, '/model', 3),
(0, '知识库管理', 'knowledge', 1, '/knowledge', 4),
(0, '应用集成', 'integration', 1, '/integration', 5);

-- 绑定管理员角色
INSERT INTO t_user_role (user_id, role_id) VALUES (1, 1);

-- 插入默认知识分类
INSERT INTO t_knowledge_category (parent_id, category_name, category_code, category_type, description)
VALUES
(0, '数据库脚本', 'database', 1, '数据库相关脚本和文档'),
(0, '业务知识', 'business', 2, '业务流程和规则文档'),
(0, '技术文档', 'technical', 3, '技术架构和开发文档'),
(0, '其他资料', 'other', 4, '其他类型的资料');

