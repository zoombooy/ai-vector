-- 修复模型配置中的 max_tokens 值
USE ai_knowledge_platform;

-- 更新所有模型的 max_tokens 为合理的默认值（2000）
UPDATE t_model_config 
SET max_tokens = 2000 
WHERE max_tokens IS NULL OR max_tokens < 100;

-- 查看更新后的配置
SELECT 
    mc.id,
    am.model_name,
    am.model_code,
    mc.max_tokens,
    mc.temperature,
    mc.top_p
FROM t_model_config mc
LEFT JOIN t_ai_model am ON mc.model_id = am.id;

