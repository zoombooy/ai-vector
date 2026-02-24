package com.aimanager.model.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.model.dto.AiModelDTO;
import com.aimanager.model.dto.AiModelVO;
import com.aimanager.model.entity.AiModel;
import com.aimanager.model.entity.ModelConfig;
import com.aimanager.model.mapper.AiModelMapper;
import com.aimanager.model.mapper.ModelConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI模型服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiModelService {

    private final AiModelMapper aiModelMapper;
    private final ModelConfigMapper modelConfigMapper;
    
    /**
     * 分页查询模型（返回VO，包含配置信息）
     */
    public Page<AiModelVO> pageModels(Integer pageNum, Integer pageSize, String keyword, String modelType) {
        Page<AiModel> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiModel::getModelName, keyword)
                    .or().like(AiModel::getModelCode, keyword));
        }

        if (StringUtils.hasText(modelType)) {
            wrapper.eq(AiModel::getModelType, modelType);
        }

        wrapper.orderByDesc(AiModel::getCreateTime);

        Page<AiModel> modelPage = aiModelMapper.selectPage(page, wrapper);

        // 转换为VO并填充配置信息
        Page<AiModelVO> voPage = new Page<>();
        voPage.setCurrent(modelPage.getCurrent());
        voPage.setSize(modelPage.getSize());
        voPage.setTotal(modelPage.getTotal());
        voPage.setPages(modelPage.getPages());

        List<AiModelVO> voList = modelPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }
    
    /**
     * 获取所有启用的模型
     */
    public List<AiModel> getEnabledModels() {
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getStatus, 1);
        wrapper.orderByDesc(AiModel::getCreateTime);
        return aiModelMapper.selectList(wrapper);
    }
    
    /**
     * 根据ID获取模型（包含配置信息）
     */
    public AiModelVO getModelById(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }
        return convertToVO(model);
    }

    /**
     * 创建模型（包含配置）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createModel(AiModelDTO dto) {
        // 检查模型编码是否存在
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getModelCode, dto.getModelCode());
        if (aiModelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.MODEL_ALREADY_EXISTS);
        }

        // 创建模型
        AiModel model = new AiModel();
        BeanUtils.copyProperties(dto, model);
        if (model.getStatus() == null) {
            model.setStatus(1); // 默认启用
        }
        aiModelMapper.insert(model);

        // 创建配置
        ModelConfig config = new ModelConfig();
        config.setModelId(model.getId());
        config.setApiUrl(dto.getApiUrl());
        config.setApiKey(encryptApiKey(dto.getApiKey())); // 加密存储
        config.setMaxTokens(dto.getMaxTokens());
        config.setTemperature(dto.getTemperature());
        config.setTopP(dto.getTopP());
        config.setFrequencyPenalty(dto.getFrequencyPenalty());
        config.setPresencePenalty(dto.getPresencePenalty());
        config.setTimeout(dto.getTimeout() != null ? dto.getTimeout() : 30);
        config.setRetryTimes(dto.getRetryTimes() != null ? dto.getRetryTimes() : 3);
        config.setConfigJson(dto.getConfigJson());
        modelConfigMapper.insert(config);

        log.info("创建AI模型成功: {}", model.getModelName());
    }

    /**
     * 更新模型（包含配置）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateModel(AiModelDTO dto) {
        AiModel existModel = aiModelMapper.selectById(dto.getId());
        if (existModel == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        // 检查模型编码是否被其他模型使用
        LambdaQueryWrapper<AiModel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModel::getModelCode, dto.getModelCode())
                .ne(AiModel::getId, dto.getId());
        if (aiModelMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.MODEL_ALREADY_EXISTS);
        }

        // 更新模型
        AiModel model = new AiModel();
        BeanUtils.copyProperties(dto, model);
        aiModelMapper.updateById(model);

        // 更新配置
        LambdaQueryWrapper<ModelConfig> configWrapper = new LambdaQueryWrapper<>();
        configWrapper.eq(ModelConfig::getModelId, dto.getId());
        ModelConfig existConfig = modelConfigMapper.selectOne(configWrapper);

        if (existConfig != null) {
            // 更新现有配置
            existConfig.setApiUrl(dto.getApiUrl());
            if (StringUtils.hasText(dto.getApiKey()) && !dto.getApiKey().contains("***")) {
                existConfig.setApiKey(encryptApiKey(dto.getApiKey()));
            }
            existConfig.setMaxTokens(dto.getMaxTokens());
            existConfig.setTemperature(dto.getTemperature());
            existConfig.setTopP(dto.getTopP());
            existConfig.setFrequencyPenalty(dto.getFrequencyPenalty());
            existConfig.setPresencePenalty(dto.getPresencePenalty());
            existConfig.setTimeout(dto.getTimeout());
            existConfig.setRetryTimes(dto.getRetryTimes());
            existConfig.setConfigJson(dto.getConfigJson());
            modelConfigMapper.updateById(existConfig);
        } else {
            // 创建新配置
            ModelConfig config = new ModelConfig();
            config.setModelId(dto.getId());
            config.setApiUrl(dto.getApiUrl());
            config.setApiKey(encryptApiKey(dto.getApiKey()));
            config.setMaxTokens(dto.getMaxTokens());
            config.setTemperature(dto.getTemperature());
            config.setTopP(dto.getTopP());
            config.setFrequencyPenalty(dto.getFrequencyPenalty());
            config.setPresencePenalty(dto.getPresencePenalty());
            config.setTimeout(dto.getTimeout() != null ? dto.getTimeout() : 30);
            config.setRetryTimes(dto.getRetryTimes() != null ? dto.getRetryTimes() : 3);
            config.setConfigJson(dto.getConfigJson());
            modelConfigMapper.insert(config);
        }

        log.info("更新AI模型成功: {}", model.getModelName());
    }

    /**
     * 删除模型（同时删除配置）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteModel(Long id) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        // 删除模型
        aiModelMapper.deleteById(id);

        // 删除配置
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getModelId, id);
        modelConfigMapper.delete(wrapper);

        log.info("删除AI模型成功: {}", model.getModelName());
    }

    /**
     * 转换为VO
     */
    private AiModelVO convertToVO(AiModel model) {
        AiModelVO vo = new AiModelVO();
        BeanUtils.copyProperties(model, vo);

        // 查询配置信息
        LambdaQueryWrapper<ModelConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelConfig::getModelId, model.getId());
        ModelConfig config = modelConfigMapper.selectOne(wrapper);

        if (config != null) {
            vo.setConfigId(config.getId());
            vo.setApiUrl(config.getApiUrl());
            vo.setApiKey(maskApiKey(config.getApiKey())); // 脱敏显示
            vo.setMaxTokens(config.getMaxTokens());
            vo.setTemperature(config.getTemperature());
            vo.setTopP(config.getTopP());
            vo.setFrequencyPenalty(config.getFrequencyPenalty());
            vo.setPresencePenalty(config.getPresencePenalty());
            vo.setTimeout(config.getTimeout());
            vo.setRetryTimes(config.getRetryTimes());
            vo.setConfigJson(config.getConfigJson());
        }

        return vo;
    }

    /**
     * 加密API密钥（简单实现，实际应使用更安全的加密方式）
     */
    private String encryptApiKey(String apiKey) {
        // TODO: 实现真正的加密逻辑
        return apiKey;
    }

    /**
     * 脱敏显示API密钥
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 更新模型状态
     */
    public void updateStatus(Long id, Integer status) {
        AiModel model = aiModelMapper.selectById(id);
        if (model == null) {
            throw new BusinessException(ResultCode.MODEL_NOT_EXIST);
        }

        model.setStatus(status);
        aiModelMapper.updateById(model);
        log.info("更新模型状态成功: {} -> {}", model.getModelName(), status == 1 ? "启用" : "禁用");
    }
}

