package com.aimanager.qa.mapper;

import com.aimanager.qa.entity.AgentExecutionHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Agent执行历史 Mapper
 */
@Mapper
public interface AgentExecutionHistoryMapper extends BaseMapper<AgentExecutionHistory> {
}

