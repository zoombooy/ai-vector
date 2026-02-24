package com.aimanager.qa.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aimanager.qa.dto.AgentExecuteRequest;
import com.aimanager.qa.dto.AgentExecuteResponse;
import com.aimanager.qa.dto.AgentVO;
import com.aimanager.qa.entity.AgentExecutionHistory;
import com.aimanager.qa.entity.ExternalAgent;
import com.aimanager.qa.mapper.AgentExecutionHistoryMapper;
import com.aimanager.qa.mapper.ExternalAgentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Agent服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {
    
    private final ExternalAgentMapper agentMapper;
    private final AgentExecutionHistoryMapper historyMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 分页查询Agent
     */
    public Page<AgentVO> pageAgents(Integer pageNum, Integer pageSize, String keyword, String category) {
        Page<ExternalAgent> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<ExternalAgent> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(ExternalAgent::getAgentName, keyword)
                    .or().like(ExternalAgent::getAgentCode, keyword)
                    .or().like(ExternalAgent::getDescription, keyword));
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(ExternalAgent::getCategory, category);
        }
        wrapper.eq(ExternalAgent::getDeleted, 0);
        wrapper.orderByDesc(ExternalAgent::getPriority, ExternalAgent::getCreateTime);

        Page<ExternalAgent> result = agentMapper.selectPage(page, wrapper);

        List<AgentVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        Page<AgentVO> pageResult = new Page<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setPages(pageNum);
        pageResult.setSize(pageSize);
        return pageResult;
    }
    
    /**
     * 根据ID获取Agent
     */
    public AgentVO getAgentById(Long id) {
        ExternalAgent agent = agentMapper.selectById(id);
        if (agent == null) {
            throw new RuntimeException("Agent不存在");
        }
        return convertToVO(agent);
    }
    
    /**
     * 根据编码获取Agent
     */
    public AgentVO getAgentByCode(String agentCode) {
        ExternalAgent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<ExternalAgent>()
                        .eq(ExternalAgent::getAgentCode, agentCode)
                        .eq(ExternalAgent::getDeleted, 0)
        );
        if (agent == null) {
            throw new RuntimeException("Agent不存在: " + agentCode);
        }
        return convertToVO(agent);
    }

    /**
     * 根据编码获取Agent实体（用于内部调用）
     */
    public ExternalAgent getByCode(String agentCode) {
        return agentMapper.selectOne(
                new LambdaQueryWrapper<ExternalAgent>()
                        .eq(ExternalAgent::getAgentCode, agentCode)
                        .eq(ExternalAgent::getDeleted, 0)
        );
    }
    
    /**
     * 创建Agent
     */
    public Long createAgent(ExternalAgent agent) {
        // 检查编码是否重复
        Long count = agentMapper.selectCount(
                new LambdaQueryWrapper<ExternalAgent>()
                        .eq(ExternalAgent::getAgentCode, agent.getAgentCode())
                        .eq(ExternalAgent::getDeleted, 0)
        );
        if (count > 0) {
            throw new RuntimeException("Agent编码已存在: " + agent.getAgentCode());
        }
        
        agentMapper.insert(agent);
        return agent.getId();
    }
    
    /**
     * 更新Agent
     */
    public void updateAgent(ExternalAgent agent) {
        agentMapper.updateById(agent);
    }
    
    /**
     * 删除Agent
     */
    public void deleteAgent(Long id) {
        agentMapper.deleteById(id);
    }
    
    /**
     * 执行Agent
     */
    public AgentExecuteResponse executeAgent(AgentExecuteRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始执行Agent: agentCode={}, input={}", request.getAgentCode(), request.getInput());
        
        // 查询Agent定义
        ExternalAgent agent = agentMapper.selectOne(
                new LambdaQueryWrapper<ExternalAgent>()
                        .eq(ExternalAgent::getAgentCode, request.getAgentCode())
                        .eq(ExternalAgent::getStatus, 1)
                        .eq(ExternalAgent::getDeleted, 0)
        );
        
        if (agent == null) {
            throw new RuntimeException("Agent不存在或未启用: " + request.getAgentCode());
        }

        // 创建执行历史记录
        AgentExecutionHistory history = new AgentExecutionHistory();
        history.setSessionId(request.getSessionId());
        history.setAgentId(agent.getId());
        history.setAgentCode(agent.getAgentCode());
        history.setUserId(request.getUserId());
        history.setStartTime(LocalDateTime.now());
        history.setStatus("RUNNING");

        try {
            history.setInputData(objectMapper.writeValueAsString(request.getInput()));
        } catch (Exception e) {
            log.error("序列化输入数据失败", e);
        }

        historyMapper.insert(history);

        AgentExecuteResponse response = new AgentExecuteResponse();
        response.setExecutionId(history.getId());
        response.setAgentCode(agent.getAgentCode());

        try {
            // 构建请求
            String url = agent.getEndpointUrl();
            HttpHeaders headers = buildHeaders(agent);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("input", request.getInput());
            requestBody.put("agent_code", agent.getAgentCode());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用Agent服务: url={}, input={}", url, request.getInput());

            // 执行HTTP请求
            ResponseEntity<String> httpResponse = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (httpResponse.getStatusCode() == HttpStatus.OK) {
                String responseBody = httpResponse.getBody();
                log.info("Agent执行成功: response={}", responseBody);

                // 解析响应
                Map<String, Object> result = objectMapper.readValue(
                        responseBody,
                        new TypeReference<Map<String, Object>>() {}
                );

                response.setStatus("SUCCESS");
                response.setOutput(result);

                // 更新历史记录
                history.setStatus("SUCCESS");
                history.setOutputData(responseBody);
            } else {
                throw new RuntimeException("Agent执行失败，HTTP状态码: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Agent执行异常: agentCode={}", request.getAgentCode(), e);

            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());

            // 更新历史记录
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
        } finally {
            // 计算执行时间
            long executionTime = System.currentTimeMillis() - startTime;
            history.setEndTime(LocalDateTime.now());
            history.setExecutionTime(executionTime);

            response.setExecutionTime(executionTime);
            response.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            // 更新历史记录
            historyMapper.updateById(history);
        }

        return response;
    }

    /**
     * 获取所有启用的Agent（用于AI调用）
     */
    public List<Map<String, Object>> getAvailableAgents() {
        List<ExternalAgent> agents = agentMapper.selectList(
                new LambdaQueryWrapper<ExternalAgent>()
                        .eq(ExternalAgent::getStatus, 1)
                        .eq(ExternalAgent::getDeleted, 0)
                        .orderByDesc(ExternalAgent::getPriority)
        );

        return agents.stream().map(this::convertToAgentDefinition).collect(Collectors.toList());
    }

    /**
     * 获取Agent执行历史
     */
    public List<AgentExecutionHistory> getExecutionHistory(String sessionId, String agentCode) {
        LambdaQueryWrapper<AgentExecutionHistory> wrapper = new LambdaQueryWrapper<>();

        if (sessionId != null && !sessionId.isEmpty()) {
            wrapper.eq(AgentExecutionHistory::getSessionId, sessionId);
        }
        if (agentCode != null && !agentCode.isEmpty()) {
            wrapper.eq(AgentExecutionHistory::getAgentCode, agentCode);
        }

        wrapper.orderByDesc(AgentExecutionHistory::getCreateTime);
        wrapper.last("LIMIT 100");

        return historyMapper.selectList(wrapper);
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHeaders(ExternalAgent agent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 处理认证
        if ("BEARER".equalsIgnoreCase(agent.getAuthType())) {
            try {
                Map<String, String> authConfig = objectMapper.readValue(
                        agent.getAuthConfig(),
                        new TypeReference<Map<String, String>>() {}
                );
                String token = authConfig.get("token");
                if (token != null) {
                    headers.setBearerAuth(token);
                }
            } catch (Exception e) {
                log.error("解析认证配置失败", e);
            }
        } else if ("API_KEY".equalsIgnoreCase(agent.getAuthType())) {
            try {
                Map<String, String> authConfig = objectMapper.readValue(
                        agent.getAuthConfig(),
                        new TypeReference<Map<String, String>>() {}
                );
                String apiKey = authConfig.get("api_key");
                String headerName = authConfig.getOrDefault("header_name", "X-API-Key");
                if (apiKey != null) {
                    headers.set(headerName, apiKey);
                }
            } catch (Exception e) {
                log.error("解析认证配置失败", e);
            }
        }

        return headers;
    }

    /**
     * 转换为VO对象
     */
    private AgentVO convertToVO(ExternalAgent agent) {
        AgentVO vo = new AgentVO();
        vo.setId(agent.getId());
        vo.setAgentName(agent.getAgentName());
        vo.setAgentCode(agent.getAgentCode());
        vo.setAgentType(agent.getAgentType());
        vo.setDescription(agent.getDescription());
        vo.setEndpointUrl(agent.getEndpointUrl());
        vo.setTimeout(agent.getTimeout());
        vo.setStatus(agent.getStatus());
        vo.setVersion(agent.getVersion());
        vo.setCategory(agent.getCategory());
        vo.setTags(agent.getTags());
        vo.setPriority(agent.getPriority());
        vo.setCreateTime(agent.getCreateTime());
        vo.setUpdateTime(agent.getUpdateTime());

        // 解析JSON字段
        try {
            if (agent.getInputSchema() != null) {
                vo.setInputSchema(objectMapper.readValue(agent.getInputSchema(), new TypeReference<Map<String, Object>>() {}));
            }
            if (agent.getOutputSchema() != null) {
                vo.setOutputSchema(objectMapper.readValue(agent.getOutputSchema(), new TypeReference<Map<String, Object>>() {}));
            }
            if (agent.getCapabilities() != null) {
                vo.setCapabilities(objectMapper.readValue(agent.getCapabilities(), new TypeReference<List<String>>() {}));
            }
        } catch (Exception e) {
            log.error("解析JSON字段失败", e);
        }

        return vo;
    }

    /**
     * 转换为Agent定义（用于AI调用）
     */
    private Map<String, Object> convertToAgentDefinition(ExternalAgent agent) {
        Map<String, Object> definition = new HashMap<>();
        definition.put("name", agent.getAgentCode());
        definition.put("description", agent.getDescription());

        try {
            if (agent.getInputSchema() != null) {
                definition.put("parameters", objectMapper.readValue(agent.getInputSchema(), Map.class));
            }
        } catch (Exception e) {
            log.error("解析输入Schema失败", e);
        }

        return definition;
    }

    /**
     * 执行Agent（通过Function Call方式）
     * @param agentCode Agent编码
     * @param arguments 参数（JSON字符串）
     * @param sessionId 会话ID
     * @return 执行结果（JSON字符串）
     */
    public String executeAgentByFunctionCall(String agentCode, String arguments, String sessionId) {
        try {
            // 解析参数
            Map<String, Object> input = objectMapper.readValue(arguments,
                    new TypeReference<Map<String, Object>>() {});

            // 构建执行请求
            AgentExecuteRequest request = new AgentExecuteRequest();
            request.setAgentCode(agentCode);
            request.setInput(input);
            request.setSessionId(sessionId);

            // 执行Agent
            AgentExecuteResponse response = executeAgent(request);

            // 返回结果
            if ("SUCCESS".equals(response.getStatus())) {
                return objectMapper.writeValueAsString(response.getOutput());
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", response.getErrorMessage());
                error.put("status", response.getStatus());
                return objectMapper.writeValueAsString(error);
            }
        } catch (Exception e) {
            log.error("执行Agent失败: agentCode={}, error={}", agentCode, e.getMessage(), e);
            try {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                return objectMapper.writeValueAsString(error);
            } catch (Exception ex) {
                return "{\"error\": \"执行失败\"}";
            }
        }
    }
}

