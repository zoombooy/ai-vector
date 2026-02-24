-- 清理问答历史中的无效数据
USE ai_knowledge_platform;

-- 查看有多少条 answer 为 null 的记录
SELECT COUNT(*) as null_answer_count
FROM t_qa_history
WHERE answer IS NULL OR answer = '';

-- 删除 answer 为 null 或空的记录
DELETE FROM t_qa_history
WHERE answer IS NULL OR answer = '';

-- 查看剩余的记录
SELECT 
    id,
    session_id,
    question,
    SUBSTRING(answer, 1, 50) as answer_preview,
    model_name,
    create_time
FROM t_qa_history
ORDER BY create_time DESC
LIMIT 20;

