package com.aimanager.model.controller;

import com.aimanager.common.result.Result;
import com.aimanager.model.dto.AiModelDTO;
import com.aimanager.model.dto.AiModelVO;
import com.aimanager.model.entity.AiModel;
import com.aimanager.model.service.AiModelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI模型管理控制器
 */
@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
public class AiModelController {

    private final AiModelService aiModelService;

    /**
     * 分页查询模型
     */
    @GetMapping("/page")
    public Result<Page<AiModelVO>> pageModels(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "modelType", required = false) String modelType) {

        Page<AiModelVO> page = aiModelService.pageModels(pageNum, pageSize, keyword, modelType);
        return Result.success(page);
    }

    /**
     * 获取所有启用的模型
     */
    @GetMapping("/enabled")
    public Result<List<AiModel>> getEnabledModels() {
        List<AiModel> models = aiModelService.getEnabledModels();
        return Result.success(models);
    }

    /**
     * 根据ID获取模型详情（包含配置）
     */
    @GetMapping("/{id}")
    public Result<AiModelVO> getModelById(@PathVariable("id") Long id) {
        AiModelVO model = aiModelService.getModelById(id);
        return Result.success(model);
    }

    /**
     * 创建模型
     */
    @PostMapping
    public Result<Void> createModel(@Validated @RequestBody AiModelDTO dto) {
        aiModelService.createModel(dto);
        return Result.success();
    }

    /**
     * 更新模型
     */
    @PutMapping
    public Result<Void> updateModel(@Validated @RequestBody AiModelDTO dto) {
        aiModelService.updateModel(dto);
        return Result.success();
    }

    /**
     * 删除模型
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteModel(@PathVariable("id") Long id) {
        aiModelService.deleteModel(id);
        return Result.success();
    }

    /**
     * 批量删除模型
     */
    @DeleteMapping("/batch")
    public Result<Void> batchDeleteModels(@RequestBody List<Long> ids) {
        ids.forEach(aiModelService::deleteModel);
        return Result.success();
    }

    /**
     * 启用/禁用模型
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable("id") Long id, @RequestParam("status") Integer status) {
        aiModelService.updateStatus(id, status);
        return Result.success();
    }
}

