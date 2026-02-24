package com.aimanager.knowledge.mapper;

import com.aimanager.knowledge.entity.KnowledgeCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识分类Mapper
 */
@Mapper
public interface KnowledgeCategoryMapper extends BaseMapper<KnowledgeCategory> {
}

