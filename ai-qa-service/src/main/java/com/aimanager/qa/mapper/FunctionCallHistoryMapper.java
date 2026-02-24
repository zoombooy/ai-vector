package com.aimanager.qa.mapper;

import com.aimanager.qa.entity.FunctionCallHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 函数调用历史 Mapper
 */
@Mapper
public interface FunctionCallHistoryMapper extends BaseMapper<FunctionCallHistory> {
}

