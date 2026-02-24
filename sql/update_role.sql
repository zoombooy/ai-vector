-- 更新角色表结构
-- 添加缺失的字段

-- 1. 添加 sort 字段（排序）
ALTER TABLE t_role ADD COLUMN sort INT DEFAULT 0 COMMENT '排序' AFTER description;

-- 2. 添加 status 字段（状态）
ALTER TABLE t_role ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）' AFTER sort;

-- 3. 删除不需要的字段（如果存在）
ALTER TABLE t_role DROP COLUMN IF EXISTS role_type;

-- 4. 添加索引
ALTER TABLE t_role ADD INDEX idx_status (status);

