package com.aimanager.qa.controller;

import com.aimanager.common.result.Result;
import com.aimanager.qa.entity.ExternalFunction;
import com.aimanager.qa.entity.FunctionCallHistory;
import com.aimanager.qa.mapper.ExternalFunctionMapper;
import com.aimanager.qa.mapper.FunctionCallHistoryMapper;
import com.aimanager.qa.service.FunctionCallService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 外部函数管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/function")
@RequiredArgsConstructor
public class FunctionController {
    
    private final ExternalFunctionMapper functionMapper;
    private final FunctionCallHistoryMapper historyMapper;
    private final FunctionCallService functionCallService;
    
    /**
     * 获取所有函数列表
     */
    @GetMapping("/list")
    public Result<List<ExternalFunction>> list() {
        List<ExternalFunction> functions = functionMapper.selectList(
            new LambdaQueryWrapper<ExternalFunction>()
                .eq(ExternalFunction::getDeleted, 0)
                .orderByDesc(ExternalFunction::getCreateTime)
        );
        return Result.success(functions);
    }
    
    /**
     * 分页查询函数
     */
    @GetMapping("/page")
    public Result<Page<ExternalFunction>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        
        LambdaQueryWrapper<ExternalFunction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExternalFunction::getDeleted, 0);
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ExternalFunction::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(ExternalFunction::getStatus, status);
        }
        
        wrapper.orderByDesc(ExternalFunction::getCreateTime);
        
        Page<ExternalFunction> page = new Page<>(current, size);
        functionMapper.selectPage(page, wrapper);
        
        return Result.success(page);
    }
    
    /**
     * 获取函数详情
     */
    @GetMapping("/{id}")
    public Result<ExternalFunction> getById(@PathVariable Long id) {
        ExternalFunction function = functionMapper.selectById(id);
        if (function == null || function.getDeleted() == 1) {
            return Result.fail("函数不存在");
        }
        return Result.success(function);
    }
    
    /**
     * 新增函数
     */
    @PostMapping
    public Result<Void> add(@RequestBody ExternalFunction function) {
        // 检查函数名是否重复
        Long count = functionMapper.selectCount(
            new LambdaQueryWrapper<ExternalFunction>()
                .eq(ExternalFunction::getFunctionName, function.getFunctionName())
                .eq(ExternalFunction::getDeleted, 0)
        );
        
        if (count > 0) {
            return Result.fail("函数名称已存在");
        }
        
        functionMapper.insert(function);
        return Result.success();
    }
    
    /**
     * 更新函数
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ExternalFunction function) {
        ExternalFunction existing = functionMapper.selectById(id);
        if (existing == null || existing.getDeleted() == 1) {
            return Result.fail("函数不存在");
        }
        
        function.setId(id);
        functionMapper.updateById(function);
        return Result.success();
    }
    
    /**
     * 删除函数（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ExternalFunction function = functionMapper.selectById(id);
        if (function == null) {
            return Result.fail("函数不存在");
        }
        
        function.setDeleted(1);
        functionMapper.updateById(function);
        return Result.success();
    }
    
    /**
     * 启用/禁用函数
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ExternalFunction function = functionMapper.selectById(id);
        if (function == null || function.getDeleted() == 1) {
            return Result.fail("函数不存在");
        }
        
        function.setStatus(status);
        functionMapper.updateById(function);
        return Result.success();
    }
    
    /**
     * 测试函数调用
     */
    @PostMapping("/{id}/test")
    public Result<String> testFunction(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        ExternalFunction function = functionMapper.selectById(id);
        if (function == null || function.getDeleted() == 1) {
            return Result.fail("函数不存在");
        }
        
        try {
            String paramsJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(params);
            String result = functionCallService.executeFunction(function.getFunctionName(), paramsJson, "test");
            return Result.success(result);
        } catch (Exception e) {
            log.error("测试函数调用失败", e);
            return Result.fail("测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取函数调用历史
     */
    @GetMapping("/history")
    public Result<Page<FunctionCallHistory>> getHistory(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) Long functionId) {

        LambdaQueryWrapper<FunctionCallHistory> wrapper = new LambdaQueryWrapper<>();

        if (sessionId != null && !sessionId.isEmpty()) {
            wrapper.eq(FunctionCallHistory::getSessionId, sessionId);
        }
        if (functionId != null) {
            wrapper.eq(FunctionCallHistory::getFunctionId, functionId);
        }

        wrapper.orderByDesc(FunctionCallHistory::getCreateTime);

        Page<FunctionCallHistory> page = new Page<>(current, size);
        historyMapper.selectPage(page, wrapper);

        return Result.success(page);
    }

    /**
     * 获取可用的函数定义（用于前端展示）
     */
    @GetMapping("/available")
    public Result<List<Map<String, Object>>> getAvailableFunctions() {
        List<Map<String, Object>> functions = functionCallService.getAvailableFunctions();
        return Result.success(functions);
    }
}
