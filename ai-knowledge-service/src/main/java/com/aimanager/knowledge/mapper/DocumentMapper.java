package com.aimanager.knowledge.mapper;

import com.aimanager.knowledge.entity.Document;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {
}

