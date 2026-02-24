package com.aimanager.qa.mapper;

import com.aimanager.qa.entity.QaHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问答历史Mapper
 */
@Mapper
public interface QaHistoryMapper extends BaseMapper<QaHistory> {
}

