-- 更新组织表结构
-- 添加缺失的字段

-- 1. 添加 org_level 字段（组织层级）
ALTER TABLE t_organization ADD COLUMN org_level INT DEFAULT 1 COMMENT '组织层级' AFTER parent_id;

-- 2. 添加 org_path 字段（组织路径）
ALTER TABLE t_organization ADD COLUMN org_path VARCHAR(500) COMMENT '组织路径' AFTER org_level;

-- 3. 添加 leader_id 字段（负责人ID）
ALTER TABLE t_organization ADD COLUMN leader_id BIGINT COMMENT '负责人ID' AFTER org_code;

-- 4. 添加 status 字段（状态）
ALTER TABLE t_organization ADD COLUMN status TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）' AFTER leader_id;

-- 5. 添加 remark 字段（备注）
ALTER TABLE t_organization ADD COLUMN remark VARCHAR(500) COMMENT '备注' AFTER status;

-- 6. 修改 sort_order 字段名为 sort
ALTER TABLE t_organization CHANGE COLUMN sort_order sort INT DEFAULT 0 COMMENT '排序';

-- 7. 删除不需要的字段（如果存在）
ALTER TABLE t_organization DROP COLUMN IF EXISTS org_type;
ALTER TABLE t_organization DROP COLUMN IF EXISTS description;

-- 8. 更新现有数据的 org_level 和 org_path
UPDATE t_organization SET org_level = 1, org_path = CONCAT('/', id, '/') WHERE parent_id = 0;

-- 9. 添加索引
ALTER TABLE t_organization ADD INDEX idx_leader_id (leader_id);
ALTER TABLE t_organization ADD INDEX idx_status (status);

