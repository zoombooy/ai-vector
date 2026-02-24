-- 工作流定义表
CREATE TABLE IF NOT EXISTS `t_workflow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workflow_name` VARCHAR(100) NOT NULL COMMENT '工作流名称',
    `workflow_code` VARCHAR(50) NOT NULL COMMENT '工作流编码（唯一）',
    `description` TEXT COMMENT '描述',
    `category` VARCHAR(50) COMMENT '分类',
    `tags` VARCHAR(200) COMMENT '标签（逗号分隔）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    `version` VARCHAR(20) DEFAULT '1.0.0' COMMENT '版本号',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` VARCHAR(50) COMMENT '创建人',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` VARCHAR(50) COMMENT '更新人',
    `deleted` TINYINT DEFAULT 0 COMMENT '删除标记：0-未删除，1-已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_workflow_code` (`workflow_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流定义表';

-- 工作流节点表
CREATE TABLE IF NOT EXISTS `t_workflow_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workflow_id` BIGINT NOT NULL COMMENT '工作流ID',
    `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID（前端生成的唯一ID）',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `node_type` VARCHAR(20) NOT NULL COMMENT '节点类型：AGENT/FUNCTION/CONDITION/LOOP/START/END',
    `position_x` INT COMMENT 'X坐标',
    `position_y` INT COMMENT 'Y坐标',
    `config` TEXT COMMENT '节点配置（JSON格式）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_id` (`workflow_id`),
    UNIQUE KEY `uk_workflow_node` (`workflow_id`, `node_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点表';

-- 工作流连线表
CREATE TABLE IF NOT EXISTS `t_workflow_edge` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workflow_id` BIGINT NOT NULL COMMENT '工作流ID',
    `edge_id` VARCHAR(50) NOT NULL COMMENT '连线ID（前端生成的唯一ID）',
    `source_node_id` VARCHAR(50) NOT NULL COMMENT '源节点ID',
    `target_node_id` VARCHAR(50) NOT NULL COMMENT '目标节点ID',
    `condition` TEXT COMMENT '条件表达式（用于条件分支）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_id` (`workflow_id`),
    UNIQUE KEY `uk_workflow_edge` (`workflow_id`, `edge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流连线表';

-- 工作流执行历史表
CREATE TABLE IF NOT EXISTS `t_workflow_execution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `workflow_id` BIGINT NOT NULL COMMENT '工作流ID',
    `workflow_code` VARCHAR(50) NOT NULL COMMENT '工作流编码',
    `session_id` VARCHAR(100) COMMENT '会话ID',
    `input_data` TEXT COMMENT '输入数据（JSON格式）',
    `output_data` TEXT COMMENT '输出数据（JSON格式）',
    `status` VARCHAR(20) NOT NULL COMMENT '执行状态：RUNNING/SUCCESS/FAILED/CANCELLED',
    `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration` INT COMMENT '执行时长（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_workflow_id` (`workflow_id`),
    KEY `idx_session_id` (`session_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流执行历史表';

-- 工作流节点执行历史表
CREATE TABLE IF NOT EXISTS `t_workflow_node_execution` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `execution_id` BIGINT NOT NULL COMMENT '工作流执行ID',
    `node_id` VARCHAR(50) NOT NULL COMMENT '节点ID',
    `node_name` VARCHAR(100) NOT NULL COMMENT '节点名称',
    `node_type` VARCHAR(20) NOT NULL COMMENT '节点类型',
    `input_data` TEXT COMMENT '输入数据（JSON格式）',
    `output_data` TEXT COMMENT '输出数据（JSON格式）',
    `status` VARCHAR(20) NOT NULL COMMENT '执行状态：RUNNING/SUCCESS/FAILED/SKIPPED',
    `start_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    `end_time` DATETIME COMMENT '结束时间',
    `duration` INT COMMENT '执行时长（毫秒）',
    `error_message` TEXT COMMENT '错误信息',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_execution_id` (`execution_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作流节点执行历史表';

