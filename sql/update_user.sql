-- 更新用户表结构
-- 添加缺失的字段

-- 1. 添加 organization_id 字段（组织ID）
ALTER TABLE t_user ADD COLUMN organization_id BIGINT COMMENT '组织ID' AFTER avatar;

-- 2. 添加索引
ALTER TABLE t_user ADD INDEX idx_organization_id (organization_id);

