package com.aimanager.qa.service;

import com.aimanager.qa.dto.*;
import com.aimanager.qa.entity.McpServer;
import com.aimanager.qa.entity.McpTool;
import com.aimanager.qa.mapper.McpServerMapper;
import com.aimanager.qa.mapper.McpToolMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * MCP服务 - 管理MCP Server和工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final McpServerMapper serverMapper;
    private final McpToolMapper toolMapper;
    private final McpClientService mcpClientService;
    private final ObjectMapper objectMapper;

    /**
     * 分页查询MCP Server
     */
    public Page<McpServerVO> pageServers(Integer pageNum, Integer pageSize, String keyword, String category) {
        Page<McpServer> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<McpServer> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(McpServer::getServerName, keyword)
                    .or().like(McpServer::getServerCode, keyword)
                    .or().like(McpServer::getDescription, keyword));
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(McpServer::getCategory, category);
        }
        wrapper.eq(McpServer::getDeleted, 0);
        wrapper.orderByDesc(McpServer::getPriority, McpServer::getCreateTime);

        Page<McpServer> result = serverMapper.selectPage(page, wrapper);

        List<McpServerVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        Page<McpServerVO> pageResult = new Page<>();
        pageResult.setRecords(voList);
        pageResult.setTotal(result.getTotal());
        pageResult.setCurrent(pageNum);
        pageResult.setSize(pageSize);
        return pageResult;
    }

    /**
     * 获取MCP Server详情
     */
    public McpServerVO getServerById(Long id) {
        McpServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new RuntimeException("MCP Server不存在");
        }
        McpServerVO vo = convertToVO(server);

        // 加载工具列表
        List<McpTool> tools = toolMapper.selectList(
                new LambdaQueryWrapper<McpTool>()
                        .eq(McpTool::getServerId, id)
                        .eq(McpTool::getDeleted, 0)
        );
        vo.setTools(tools.stream().map(this::convertToolToVO).collect(Collectors.toList()));

        return vo;
    }

    /**
     * 根据编码获取MCP Server
     */
    public McpServer getServerByCode(String serverCode) {
        return serverMapper.selectOne(
                new LambdaQueryWrapper<McpServer>()
                        .eq(McpServer::getServerCode, serverCode)
                        .eq(McpServer::getDeleted, 0)
        );
    }

    /**
     * 创建MCP Server
     */
    @Transactional
    public Long createServer(McpServerCreateRequest request) {
        // 检查编码是否重复
        Long count = serverMapper.selectCount(
                new LambdaQueryWrapper<McpServer>()
                        .eq(McpServer::getServerCode, request.getServerCode())
                        .eq(McpServer::getDeleted, 0)
        );
        if (count > 0) {
            throw new RuntimeException("Server编码已存在: " + request.getServerCode());
        }

        McpServer server = new McpServer();
        server.setServerName(request.getServerName());
        server.setServerCode(request.getServerCode());
        server.setDescription(request.getDescription());
        server.setMcpUrl(request.getMcpUrl());
        server.setServerType(request.getServerType() != null ? request.getServerType() : "HTTP_STREAM");
        server.setAuthType(request.getAuthType() != null ? request.getAuthType() : "NONE");
        server.setSource(request.getSource() != null ? request.getSource() : "MANUAL");
        server.setSourceId(request.getSourceId());
        server.setSourceUrl(request.getSourceUrl());
        server.setLogoUrl(request.getLogoUrl());
        server.setCategory(request.getCategory());
        server.setTags(request.getTags());
        server.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        server.setTimeout(request.getTimeout() != null ? request.getTimeout() : 30);
        server.setStatus(1);
        server.setHealthStatus("UNKNOWN");

        try {
            if (request.getEnvConfig() != null) {
                server.setEnvConfig(objectMapper.writeValueAsString(request.getEnvConfig()));
            }
            if (request.getAuthConfig() != null) {
                server.setAuthConfig(objectMapper.writeValueAsString(request.getAuthConfig()));
            }
        } catch (Exception e) {
            log.error("序列化配置失败", e);
        }

        serverMapper.insert(server);
        log.info("创建MCP Server成功: id={}, serverCode={}", server.getId(), server.getServerCode());

        // 自动发现工具
        discoverTools(server.getId());

        return server.getId();
    }

    /**
     * 更新MCP Server
     */
    @Transactional
    public void updateServer(Long id, McpServerCreateRequest request) {
        McpServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new RuntimeException("MCP Server不存在");
        }

        server.setServerName(request.getServerName());
        server.setDescription(request.getDescription());
        server.setMcpUrl(request.getMcpUrl());
        if (request.getServerType() != null) {
            server.setServerType(request.getServerType());
        }
        if (request.getAuthType() != null) {
            server.setAuthType(request.getAuthType());
        }
        server.setCategory(request.getCategory());
        server.setTags(request.getTags());
        if (request.getPriority() != null) {
            server.setPriority(request.getPriority());
        }
        if (request.getTimeout() != null) {
            server.setTimeout(request.getTimeout());
        }

        try {
            if (request.getEnvConfig() != null) {
                server.setEnvConfig(objectMapper.writeValueAsString(request.getEnvConfig()));
            }
            if (request.getAuthConfig() != null) {
                server.setAuthConfig(objectMapper.writeValueAsString(request.getAuthConfig()));
            }
        } catch (Exception e) {
            log.error("序列化配置失败", e);
        }

        serverMapper.updateById(server);
        log.info("更新MCP Server成功: id={}", id);
    }

    /**
     * 删除MCP Server
     */
    @Transactional
    public void deleteServer(Long id) {
        serverMapper.deleteById(id);
        // 同时删除关联的工具
        toolMapper.delete(new LambdaQueryWrapper<McpTool>().eq(McpTool::getServerId, id));
        log.info("删除MCP Server成功: id={}", id);
    }

    /**
     * 启用/禁用MCP Server
     */
    public void toggleServerStatus(Long id, Integer status) {
        McpServer server = serverMapper.selectById(id);
        if (server == null) {
            throw new RuntimeException("MCP Server不存在");
        }
        server.setStatus(status);
        serverMapper.updateById(server);
    }

    /**
     * 启用/禁用MCP工具
     */
    public void toggleToolStatus(Long toolId, Integer enabled) {
        McpTool tool = toolMapper.selectById(toolId);
        if (tool == null) {
            throw new RuntimeException("MCP工具不存在");
        }
        tool.setEnabled(enabled);
        toolMapper.updateById(tool);
    }

    /**
     * 发现/刷新MCP Server的工具列表
     */
    @Transactional
    public List<McpToolVO> discoverTools(Long serverId) {
        McpServer server = serverMapper.selectById(serverId);
        if (server == null) {
            throw new RuntimeException("MCP Server不存在");
        }

        log.info("开始发现MCP工具: serverId={}, serverCode={}", serverId, server.getServerCode());

        // 调用MCP客户端获取工具列表
        List<McpToolVO> tools = mcpClientService.listTools(server);

        // 删除旧的工具记录
        toolMapper.delete(new LambdaQueryWrapper<McpTool>().eq(McpTool::getServerId, serverId));

        // 保存新的工具记录
        for (McpToolVO toolVO : tools) {
            McpTool tool = new McpTool();
            tool.setServerId(serverId);
            tool.setToolName(toolVO.getToolName());
            tool.setToolCode(toolVO.getToolCode());
            tool.setDescription(toolVO.getDescription());
            tool.setEnabled(1);

            try {
                if (toolVO.getInputSchema() != null) {
                    tool.setInputSchema(objectMapper.writeValueAsString(toolVO.getInputSchema()));
                }
            } catch (Exception e) {
                log.error("序列化Schema失败", e);
            }

            toolMapper.insert(tool);
        }

        // 更新健康状态
        server.setHealthStatus(tools.isEmpty() ? "UNKNOWN" : "HEALTHY");
        server.setLastHealthCheck(LocalDateTime.now());
        serverMapper.updateById(server);

        log.info("发现MCP工具完成: serverId={}, 工具数量={}", serverId, tools.size());
        return tools;
    }

    /**
     * 调用MCP工具
     */
    public McpToolCallResponse callTool(McpToolCallRequest request) {
        McpServer server = getServerByCode(request.getServerCode());
        if (server == null) {
            throw new RuntimeException("MCP Server不存在: " + request.getServerCode());
        }
        if (server.getStatus() != 1) {
            throw new RuntimeException("MCP Server未启用: " + request.getServerCode());
        }

        return mcpClientService.callTool(server, request);
    }


    /**
     * 获取所有启用的MCP工具（用于AI调用）
     */
    public List<Map<String, Object>> getAvailableMcpTools() {
        List<McpServer> servers = serverMapper.selectList(
                new LambdaQueryWrapper<McpServer>()
                        .eq(McpServer::getStatus, 1)
                        .eq(McpServer::getDeleted, 0)
        );

        List<Map<String, Object>> allTools = new ArrayList<>();

        for (McpServer server : servers) {
            List<McpTool> tools = toolMapper.selectList(
                    new LambdaQueryWrapper<McpTool>()
                            .eq(McpTool::getServerId, server.getId())
                            .eq(McpTool::getEnabled, 1)
                            .eq(McpTool::getDeleted, 0)
            );

            for (McpTool tool : tools) {
                Map<String, Object> toolDef = new HashMap<>();
                toolDef.put("name", server.getServerCode() + "__" + tool.getToolCode());
                toolDef.put("description", "[MCP:" + server.getServerName() + "] " + tool.getDescription());

                try {
                    if (tool.getInputSchema() != null) {
                        toolDef.put("parameters", objectMapper.readValue(tool.getInputSchema(), Map.class));
                    }
                } catch (Exception e) {
                    log.error("解析Schema失败", e);
                }

                allTools.add(toolDef);
            }
        }

        return allTools;
    }

    /**
     * 通过Function Call方式执行MCP工具
     */
    public String executeMcpToolByFunctionCall(String fullToolName, String arguments, String sessionId) {
        try {
            String[] parts = fullToolName.split("__", 2);
            if (parts.length != 2) {
                throw new RuntimeException("无效的MCP工具名称: " + fullToolName);
            }

            String serverCode = parts[0];
            String toolName = parts[1];

            Map<String, Object> args = objectMapper.readValue(arguments,
                    new TypeReference<Map<String, Object>>() {});

            McpToolCallRequest request = new McpToolCallRequest();
            request.setServerCode(serverCode);
            request.setToolName(toolName);
            request.setArguments(args);
            request.setSessionId(sessionId);

            McpToolCallResponse response = callTool(request);

            if ("SUCCESS".equals(response.getStatus())) {
                return objectMapper.writeValueAsString(response.getResult());
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("error", response.getErrorMessage());
                return objectMapper.writeValueAsString(error);
            }
        } catch (Exception e) {
            log.error("执行MCP工具失败: toolName={}, error={}", fullToolName, e.getMessage(), e);
            try {
                Map<String, Object> error = new HashMap<>();
                error.put("error", e.getMessage());
                return objectMapper.writeValueAsString(error);
            } catch (Exception ex) {
                return "{\"error\": \"执行失败\"}";
            }
        }
    }

    private McpServerVO convertToVO(McpServer server) {
        McpServerVO vo = new McpServerVO();
        vo.setId(server.getId());
        vo.setServerName(server.getServerName());
        vo.setServerCode(server.getServerCode());
        vo.setDescription(server.getDescription());
        vo.setMcpUrl(server.getMcpUrl());
        vo.setServerType(server.getServerType());
        vo.setAuthType(server.getAuthType());
        vo.setSource(server.getSource());
        vo.setSourceId(server.getSourceId());
        vo.setSourceUrl(server.getSourceUrl());
        vo.setLogoUrl(server.getLogoUrl());
        vo.setCategory(server.getCategory());
        vo.setTags(server.getTags());
        vo.setPriority(server.getPriority());
        vo.setStatus(server.getStatus());
        vo.setTimeout(server.getTimeout());
        vo.setLastHealthCheck(server.getLastHealthCheck());
        vo.setHealthStatus(server.getHealthStatus());
        vo.setCreateTime(server.getCreateTime());
        vo.setUpdateTime(server.getUpdateTime());

        try {
            if (server.getEnvConfig() != null) {
                vo.setEnvConfig(objectMapper.readValue(server.getEnvConfig(),
                        new TypeReference<Map<String, String>>() {}));
            }
        } catch (Exception e) {
            log.error("解析环境配置失败", e);
        }

        return vo;
    }

    private McpToolVO convertToolToVO(McpTool tool) {
        McpToolVO vo = new McpToolVO();
        vo.setId(tool.getId());
        vo.setServerId(tool.getServerId());
        vo.setToolName(tool.getToolName());
        vo.setToolCode(tool.getToolCode());
        vo.setDescription(tool.getDescription());
        vo.setEnabled(tool.getEnabled());
        vo.setCreateTime(tool.getCreateTime());
        vo.setUpdateTime(tool.getUpdateTime());

        try {
            if (tool.getInputSchema() != null) {
                vo.setInputSchema(objectMapper.readValue(tool.getInputSchema(),
                        new TypeReference<Map<String, Object>>() {}));
            }
        } catch (Exception e) {
            log.error("解析Schema失败", e);
        }

        return vo;
    }
}