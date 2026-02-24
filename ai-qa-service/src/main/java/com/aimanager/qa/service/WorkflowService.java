package com.aimanager.qa.service;

import com.aimanager.qa.dto.*;
import com.aimanager.qa.entity.*;
import com.aimanager.qa.mapper.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    
    private final WorkflowMapper workflowMapper;
    private final WorkflowNodeMapper nodeMapper;
    private final WorkflowEdgeMapper edgeMapper;
    private final WorkflowExecutionMapper executionMapper;
    private final AgentService agentService;
    private final FunctionCallService functionCallService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 分页查询工作流
     */
    public Page<Workflow> getWorkflowPage(int pageNum, int pageSize, String keyword) {
        Page<Workflow> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Workflow> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Workflow::getWorkflowName, keyword)
                    .or().like(Workflow::getWorkflowCode, keyword)
                    .or().like(Workflow::getDescription, keyword));
        }
        
        wrapper.orderByDesc(Workflow::getCreateTime);
        return workflowMapper.selectPage(page, wrapper);
    }
    
    /**
     * 根据ID获取工作流详情
     */
    public WorkflowDTO getWorkflowById(Long id) {
        Workflow workflow = workflowMapper.selectById(id);
        if (workflow == null) {
            throw new RuntimeException("工作流不存在");
        }
        
        WorkflowDTO dto = new WorkflowDTO();
        dto.setId(workflow.getId());
        dto.setWorkflowName(workflow.getWorkflowName());
        dto.setWorkflowCode(workflow.getWorkflowCode());
        dto.setDescription(workflow.getDescription());
        dto.setCategory(workflow.getCategory());
        dto.setTags(workflow.getTags());
        dto.setStatus(workflow.getStatus());
        dto.setVersion(workflow.getVersion());
        
        // 查询节点
        List<WorkflowNode> nodes = nodeMapper.selectList(
                new LambdaQueryWrapper<WorkflowNode>()
                        .eq(WorkflowNode::getWorkflowId, id)
        );
        dto.setNodes(nodes.stream().map(this::convertNodeToDTO).collect(Collectors.toList()));
        
        // 查询连线
        List<WorkflowEdge> edges = edgeMapper.selectList(
                new LambdaQueryWrapper<WorkflowEdge>()
                        .eq(WorkflowEdge::getWorkflowId, id)
        );
        dto.setEdges(edges.stream().map(this::convertEdgeToDTO).collect(Collectors.toList()));
        
        return dto;
    }
    
    /**
     * 创建或更新工作流
     */
    @Transactional
    public Long saveWorkflow(WorkflowDTO dto) {
        Workflow workflow = new Workflow();
        if (dto.getId() != null) {
            workflow = workflowMapper.selectById(dto.getId());
            if (workflow == null) {
                throw new RuntimeException("工作流不存在");
            }
        }
        
        workflow.setWorkflowName(dto.getWorkflowName());
        workflow.setWorkflowCode(dto.getWorkflowCode());
        workflow.setDescription(dto.getDescription());
        workflow.setCategory(dto.getCategory());
        workflow.setTags(dto.getTags());
        workflow.setStatus(dto.getStatus());
        workflow.setVersion(dto.getVersion());
        
        if (dto.getId() == null) {
            workflowMapper.insert(workflow);
        } else {
            workflowMapper.updateById(workflow);
            // 删除旧的节点和连线
            nodeMapper.delete(new LambdaQueryWrapper<WorkflowNode>()
                    .eq(WorkflowNode::getWorkflowId, workflow.getId()));
            edgeMapper.delete(new LambdaQueryWrapper<WorkflowEdge>()
                    .eq(WorkflowEdge::getWorkflowId, workflow.getId()));
        }
        
        // 保存节点
        if (dto.getNodes() != null) {
            for (WorkflowNodeDTO nodeDTO : dto.getNodes()) {
                WorkflowNode node = new WorkflowNode();
                node.setWorkflowId(workflow.getId());
                node.setNodeId(nodeDTO.getNodeId());
                node.setNodeName(nodeDTO.getNodeName());
                node.setNodeType(nodeDTO.getNodeType());
                node.setPositionX(nodeDTO.getPositionX());
                node.setPositionY(nodeDTO.getPositionY());
                try {
                    node.setConfig(objectMapper.writeValueAsString(nodeDTO.getConfig()));
                } catch (Exception e) {
                    log.error("序列化节点配置失败", e);
                }
                nodeMapper.insert(node);
            }
        }
        
        // 保存连线
        if (dto.getEdges() != null) {
            for (WorkflowEdgeDTO edgeDTO : dto.getEdges()) {
                WorkflowEdge edge = new WorkflowEdge();
                edge.setWorkflowId(workflow.getId());
                edge.setEdgeId(edgeDTO.getEdgeId());
                edge.setSourceNodeId(edgeDTO.getSourceNodeId());
                edge.setTargetNodeId(edgeDTO.getTargetNodeId());
                edge.setCondition(edgeDTO.getCondition());
                edgeMapper.insert(edge);
            }
        }

        return workflow.getId();
    }

    /**
     * 删除工作流
     */
    @Transactional
    public void deleteWorkflow(Long id) {
        workflowMapper.deleteById(id);
        nodeMapper.delete(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getWorkflowId, id));
        edgeMapper.delete(new LambdaQueryWrapper<WorkflowEdge>()
                .eq(WorkflowEdge::getWorkflowId, id));
    }

    private WorkflowNodeDTO convertNodeToDTO(WorkflowNode node) {
        WorkflowNodeDTO dto = new WorkflowNodeDTO();
        dto.setNodeId(node.getNodeId());
        dto.setNodeName(node.getNodeName());
        dto.setNodeType(node.getNodeType());
        dto.setPositionX(node.getPositionX());
        dto.setPositionY(node.getPositionY());
        try {
            if (node.getConfig() != null) {
                dto.setConfig(objectMapper.readValue(node.getConfig(),
                        new TypeReference<Map<String, Object>>() {}));
            }
        } catch (Exception e) {
            log.error("解析节点配置失败", e);
        }
        return dto;
    }

    private WorkflowEdgeDTO convertEdgeToDTO(WorkflowEdge edge) {
        WorkflowEdgeDTO dto = new WorkflowEdgeDTO();
        dto.setEdgeId(edge.getEdgeId());
        dto.setSourceNodeId(edge.getSourceNodeId());
        dto.setTargetNodeId(edge.getTargetNodeId());
        dto.setCondition(edge.getCondition());
        return dto;
    }

    /**
     * 执行工作流
     */
    @Transactional
    public WorkflowExecuteResponse executeWorkflow(WorkflowExecuteRequest request) {
        log.info("开始执行工作流: workflowCode={}, sessionId={}", request.getWorkflowCode(), request.getSessionId());

        WorkflowExecuteResponse response = new WorkflowExecuteResponse();
        response.setSessionId(request.getSessionId());

        try {
            // 查询工作流
            Workflow workflow = workflowMapper.selectOne(
                    new LambdaQueryWrapper<Workflow>()
                            .eq(Workflow::getWorkflowCode, request.getWorkflowCode())
                            .eq(Workflow::getDeleted, 0)
            );

            if (workflow == null) {
                response.setStatus("FAILED");
                response.setErrorMessage("工作流不存在: " + request.getWorkflowCode());
                return response;
            }

            if (workflow.getStatus() != 1) {
                response.setStatus("FAILED");
                response.setErrorMessage("工作流未启用");
                return response;
            }

            // 创建执行记录
            WorkflowExecution execution = new WorkflowExecution();
            execution.setWorkflowId(workflow.getId());
            execution.setWorkflowCode(workflow.getWorkflowCode());
            execution.setSessionId(request.getSessionId());
            execution.setInputData(objectMapper.writeValueAsString(request.getInput()));
            execution.setStatus("RUNNING");
            execution.setStartTime(LocalDateTime.now());
            executionMapper.insert(execution);

            // 查询所有节点和边
            List<WorkflowNode> nodes = nodeMapper.selectList(
                    new LambdaQueryWrapper<WorkflowNode>()
                            .eq(WorkflowNode::getWorkflowId, workflow.getId())
            );

            List<WorkflowEdge> edges = edgeMapper.selectList(
                    new LambdaQueryWrapper<WorkflowEdge>()
                            .eq(WorkflowEdge::getWorkflowId, workflow.getId())
            );

            // 查找START节点
            WorkflowNode startNode = nodes.stream()
                    .filter(n -> "START".equals(n.getNodeType()))
                    .findFirst()
                    .orElse(null);

            if (startNode == null) {
                response.setStatus("FAILED");
                response.setErrorMessage("未找到START节点");
                updateExecutionStatus(execution, "FAILED", "未找到START节点", null);
                return response;
            }

            // 执行工作流
            Map<String, Object> context = new HashMap<>(request.getInput());
            executeNode(startNode, nodes, edges, context, execution.getId());

            // 更新执行记录
            updateExecutionStatus(execution, "SUCCESS", null, context);

            response.setStatus("SUCCESS");
            response.setOutput(context);

        } catch (Exception e) {
            log.error("执行工作流失败", e);
            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    /**
     * 递归执行节点
     */
    private void executeNode(WorkflowNode node, List<WorkflowNode> allNodes,
                            List<WorkflowEdge> allEdges, Map<String, Object> context,
                            Long executionId) throws Exception {
        log.info("执行节点: nodeId={}, nodeType={}", node.getNodeId(), node.getNodeType());

        // 如果是END节点，结束执行
        if ("END".equals(node.getNodeType())) {
            return;
        }

        // 执行当前节点
        Map<String, Object> nodeOutput = null;

        switch (node.getNodeType()) {
            case "START":
                // START节点不需要执行
                break;
            case "AGENT":
                nodeOutput = executeAgentNode(node, context);
                break;
            case "FUNCTION":
                nodeOutput = executeFunctionNode(node, context);
                break;
            case "CONDITION":
                // 条件节点在查找下一个节点时处理
                break;
            default:
                log.warn("未知节点类型: {}", node.getNodeType());
        }

        // 更新上下文
        if (nodeOutput != null) {
            context.putAll(nodeOutput);
        }

        // 查找下一个节点
        List<WorkflowEdge> outgoingEdges = allEdges.stream()
                .filter(e -> e.getSourceNodeId().equals(node.getNodeId()))
                .collect(Collectors.toList());

        for (WorkflowEdge edge : outgoingEdges) {
            // 检查条件
            if (edge.getCondition() != null && !edge.getCondition().isEmpty()) {
                // 简单的条件判断（可以扩展为表达式引擎）
                if (!evaluateCondition(edge.getCondition(), context)) {
                    continue;
                }
            }

            // 查找目标节点
            WorkflowNode nextNode = allNodes.stream()
                    .filter(n -> n.getNodeId().equals(edge.getTargetNodeId()))
                    .findFirst()
                    .orElse(null);

            if (nextNode != null) {
                executeNode(nextNode, allNodes, allEdges, context, executionId);
            }
        }
    }

    /**
     * 执行Agent节点
     */
    private Map<String, Object> executeAgentNode(WorkflowNode node, Map<String, Object> context) throws Exception {
        Map<String, Object> config = objectMapper.readValue(node.getConfig(),
                new TypeReference<Map<String, Object>>() {});

        String agentCode = (String) config.get("agentCode");
        Map<String, Object> input = (Map<String, Object>) config.get("input");

        // 替换输入中的变量
        Map<String, Object> resolvedInput = resolveVariables(input, context);

        // 调用Agent
        String result = agentService.executeAgentByFunctionCall(agentCode,
                objectMapper.writeValueAsString(resolvedInput),
                UUID.randomUUID().toString());

        // 解析结果
        return objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 执行Function节点
     */
    private Map<String, Object> executeFunctionNode(WorkflowNode node, Map<String, Object> context) throws Exception {
        Map<String, Object> config = objectMapper.readValue(node.getConfig(),
                new TypeReference<Map<String, Object>>() {});

        String functionName = (String) config.get("functionName");
        Map<String, Object> input = (Map<String, Object>) config.get("input");

        // 替换输入中的变量
        Map<String, Object> resolvedInput = resolveVariables(input, context);

        // 调用Function
        String result = functionCallService.executeFunction(functionName,
                objectMapper.writeValueAsString(resolvedInput),
                UUID.randomUUID().toString());

        // 解析结果
        return objectMapper.readValue(result, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * 解析变量
     */
    private Map<String, Object> resolveVariables(Map<String, Object> input, Map<String, Object> context) {
        Map<String, Object> resolved = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                String strValue = (String) value;
                // 支持 ${variableName} 格式的变量
                if (strValue.startsWith("${") && strValue.endsWith("}")) {
                    String varName = strValue.substring(2, strValue.length() - 1);
                    resolved.put(entry.getKey(), context.get(varName));
                } else {
                    resolved.put(entry.getKey(), value);
                }
            } else {
                resolved.put(entry.getKey(), value);
            }
        }
        return resolved;
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        // 简单实现：支持 key==value 格式
        if (condition.contains("==")) {
            String[] parts = condition.split("==");
            String key = parts[0].trim();
            String expectedValue = parts[1].trim().replace("\"", "");
            Object actualValue = context.get(key);
            return expectedValue.equals(String.valueOf(actualValue));
        }
        return true;
    }

    /**
     * 更新执行状态
     */
    private void updateExecutionStatus(WorkflowExecution execution, String status,
                                      String errorMessage, Map<String, Object> output) {
        try {
            execution.setStatus(status);
            execution.setEndTime(LocalDateTime.now());
            execution.setDuration(Math.toIntExact(Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis()));
            execution.setErrorMessage(errorMessage);
            if (output != null) {
                execution.setOutputData(objectMapper.writeValueAsString(output));
            }
            executionMapper.updateById(execution);
        } catch (Exception e) {
            log.error("更新执行状态失败", e);
        }
    }
}

