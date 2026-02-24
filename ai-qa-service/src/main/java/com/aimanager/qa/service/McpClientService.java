package com.aimanager.qa.service;

import com.aimanager.qa.dto.McpToolCallRequest;
import com.aimanager.qa.dto.McpToolCallResponse;
import com.aimanager.qa.dto.McpToolVO;
import com.aimanager.qa.entity.McpCallHistory;
import com.aimanager.qa.entity.McpServer;
import com.aimanager.qa.mapper.McpCallHistoryMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

/**
 * MCP客户端服务 - 负责与MCP Server通信
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpClientService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final McpCallHistoryMapper callHistoryMapper;

    /**
     * 初始化MCP会话并获取可用工具列表
     */
    public List<McpToolVO> listTools(McpServer server) {
        List<McpToolVO> tools = new ArrayList<>();

        try {
            String mcpUrl = server.getMcpUrl();
            log.info("获取MCP Server工具列表: serverCode={}, url={}", server.getServerCode(), mcpUrl);

            HttpHeaders headers = buildHeaders(server);

            // 1. 先调用initialize初始化会话
            Map<String, Object> initRequest = new HashMap<>();
            initRequest.put("jsonrpc", "2.0");
            initRequest.put("id", 1);
            initRequest.put("method", "initialize");
            Map<String, Object> initParams = new HashMap<>();
            initParams.put("protocolVersion", "2025-03-26");
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("tools", new HashMap<>());
            initParams.put("capabilities", capabilities);
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("name", "ai-knowledge-platform");
            clientInfo.put("version", "1.0.0");
            initParams.put("clientInfo", clientInfo);
            initRequest.put("params", initParams);

            HttpEntity<Map<String, Object>> initEntity = new HttpEntity<>(initRequest, headers);
            ResponseEntity<String> initResponse = restTemplate.exchange(
                    mcpUrl,
                    HttpMethod.POST,
                    initEntity,
                    String.class
            );
            log.info("MCP initialize响应: {}", initResponse.getBody());

            // 从响应头获取mcp-session-id（MCP Streamable HTTP模式需要）
            String sessionId = initResponse.getHeaders().getFirst("mcp-session-id");
            if (sessionId != null && !sessionId.isEmpty()) {
                headers.set("mcp-session-id", sessionId);
                log.info("获取到MCP Session ID: {}", sessionId);
            }

            // 2. 调用tools/list获取工具列表
            Map<String, Object> request = new HashMap<>();
            request.put("jsonrpc", "2.0");
            request.put("id", 2);
            request.put("method", "tools/list");
            request.put("params", new HashMap<>());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    mcpUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("MCP tools/list响应: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // 处理可能的多行响应（HTTP Stream模式）
                String responseBody = response.getBody();
                String[] lines = responseBody.split("\n");

                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    try {
                        JsonNode root = objectMapper.readTree(line);
                        JsonNode result = root.get("result");

                        if (result != null && result.has("tools")) {
                            JsonNode toolsNode = result.get("tools");
                            for (JsonNode toolNode : toolsNode) {
                                McpToolVO tool = new McpToolVO();
                                tool.setServerId(server.getId());
                                tool.setServerCode(server.getServerCode());
                                tool.setServerName(server.getServerName());
                                tool.setToolName(toolNode.has("name") ? toolNode.get("name").asText() : "");
                                tool.setToolCode(tool.getToolName());
                                tool.setDescription(toolNode.has("description") ? toolNode.get("description").asText() : "");

                                if (toolNode.has("inputSchema")) {
                                    tool.setInputSchema(objectMapper.convertValue(
                                            toolNode.get("inputSchema"),
                                            new TypeReference<Map<String, Object>>() {}
                                    ));
                                }
                                tool.setEnabled(1);
                                tools.add(tool);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("解析行失败(可能是SSE格式): {}", line);
                    }
                }
            }

            log.info("获取到 {} 个工具，serverCode={}", tools.size(), server.getServerCode());

        } catch (Exception e) {
            log.error("获取MCP工具列表失败: serverCode={}, error={}", server.getServerCode(), e.getMessage(), e);
        }

        return tools;
    }

    /**
     * 调用MCP工具
     */
    public McpToolCallResponse callTool(McpServer server, McpToolCallRequest request) {
        long startTime = System.currentTimeMillis();
        
        log.info("调用MCP工具: serverCode={}, toolName={}", server.getServerCode(), request.getToolName());

        // 创建调用历史记录
        McpCallHistory history = new McpCallHistory();
        history.setSessionId(request.getSessionId());
        history.setServerId(server.getId());
        history.setServerCode(server.getServerCode());
        history.setToolName(request.getToolName());
        history.setUserId(request.getUserId());
        history.setStartTime(LocalDateTime.now());
        history.setStatus("RUNNING");
        history.setCreateTime(LocalDateTime.now());
        
        try {
            history.setInputData(objectMapper.writeValueAsString(request.getArguments()));
        } catch (Exception e) {
            log.error("序列化输入数据失败", e);
        }
        
        callHistoryMapper.insert(history);

        McpToolCallResponse response = new McpToolCallResponse();
        response.setExecutionId(history.getId());
        response.setServerCode(server.getServerCode());
        response.setToolName(request.getToolName());

        try {
            HttpHeaders headers = buildHeaders(server);
            String mcpUrl = server.getMcpUrl();

            // 1. 先调用initialize初始化会话
            Map<String, Object> initRequest = new HashMap<>();
            initRequest.put("jsonrpc", "2.0");
            initRequest.put("id", 1);
            initRequest.put("method", "initialize");
            Map<String, Object> initParams = new HashMap<>();
            initParams.put("protocolVersion", "2025-03-26");
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("tools", new HashMap<>());
            initParams.put("capabilities", capabilities);
            Map<String, Object> clientInfo = new HashMap<>();
            clientInfo.put("name", "ai-knowledge-platform");
            clientInfo.put("version", "1.0.0");
            initParams.put("clientInfo", clientInfo);
            initRequest.put("params", initParams);

            HttpEntity<Map<String, Object>> initEntity = new HttpEntity<>(initRequest, headers);
            ResponseEntity<String> initResponse = restTemplate.exchange(mcpUrl, HttpMethod.POST, initEntity, String.class);

            // 从响应头获取mcp-session-id（MCP Streamable HTTP模式需要）
            String mcpSessionId = initResponse.getHeaders().getFirst("mcp-session-id");
            if (mcpSessionId != null && !mcpSessionId.isEmpty()) {
                headers.set("mcp-session-id", mcpSessionId);
                log.info("获取到MCP Session ID: {}", mcpSessionId);
            }

            // 2. 构建JSON-RPC请求 - 调用工具
            Map<String, Object> rpcRequest = new HashMap<>();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 2);
            rpcRequest.put("method", "tools/call");

            Map<String, Object> params = new HashMap<>();
            params.put("name", request.getToolName());
            params.put("arguments", request.getArguments() != null ? request.getArguments() : new HashMap<>());
            rpcRequest.put("params", params);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(rpcRequest, headers);

            log.info("调用MCP工具请求: {}", objectMapper.writeValueAsString(rpcRequest));

            ResponseEntity<String> httpResponse = restTemplate.exchange(
                    mcpUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("MCP工具调用响应: {}", httpResponse.getBody());

            if (httpResponse.getStatusCode() == HttpStatus.OK && httpResponse.getBody() != null) {
                // 处理可能的多行响应（HTTP Stream模式）
                String responseBody = httpResponse.getBody();
                String[] lines = responseBody.split("\n");

                for (String line : lines) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    try {
                        JsonNode root = objectMapper.readTree(line);

                        if (root.has("error")) {
                            JsonNode error = root.get("error");
                            String errorMsg = error.has("message") ? error.get("message").asText() : "Unknown error";
                            throw new RuntimeException("MCP调用错误: " + errorMsg);
                        }

                        JsonNode result = root.get("result");
                        if (result != null) {
                            response.setStatus("SUCCESS");
                            // 提取content
                            if (result.has("content")) {
                                JsonNode content = result.get("content");
                                if (content.isArray() && content.size() > 0) {
                                    JsonNode firstContent = content.get(0);
                                    if (firstContent.has("text")) {
                                        response.setResult(firstContent.get("text").asText());
                                    } else {
                                        response.setResult(objectMapper.convertValue(firstContent, Object.class));
                                    }
                                } else {
                                    response.setResult(objectMapper.convertValue(content, Object.class));
                                }
                            } else {
                                response.setResult(objectMapper.convertValue(result, Object.class));
                            }
                            break; // 找到结果后退出
                        }
                    } catch (Exception parseError) {
                        log.debug("解析行失败(可能是SSE格式): {}", line);
                    }
                }

                if (response.getStatus() == null) {
                    response.setStatus("SUCCESS");
                    response.setResult(responseBody);
                }

                history.setStatus("SUCCESS");
                history.setOutputData(responseBody);

                log.info("MCP工具调用成功: serverCode={}, toolName={}", server.getServerCode(), request.getToolName());
            } else {
                throw new RuntimeException("HTTP请求失败，状态码: " + httpResponse.getStatusCode());
            }

        } catch (Exception e) {
            log.error("MCP工具调用失败: serverCode={}, toolName={}, error={}",
                    server.getServerCode(), request.getToolName(), e.getMessage(), e);

            response.setStatus("FAILED");
            response.setErrorMessage(e.getMessage());

            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            history.setEndTime(LocalDateTime.now());
            history.setExecutionTime(executionTime);
            response.setExecutionTime(executionTime);

            callHistoryMapper.updateById(history);
        }

        return response;
    }

    /**
     * 构建HTTP请求头
     */
    private HttpHeaders buildHeaders(McpServer server) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // MCP服务器可能返回多种格式，需要同时接受application/json和text/event-stream
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));

        // 处理认证
        if ("BEARER".equalsIgnoreCase(server.getAuthType())) {
            try {
                Map<String, String> authConfig = objectMapper.readValue(
                        server.getAuthConfig(),
                        new TypeReference<Map<String, String>>() {}
                );
                String token = authConfig.get("token");
                if (token != null && !token.isEmpty()) {
                    headers.setBearerAuth(token);
                }
            } catch (Exception e) {
                log.error("解析认证配置失败", e);
            }
        } else if ("API_KEY".equalsIgnoreCase(server.getAuthType())) {
            try {
                Map<String, String> authConfig = objectMapper.readValue(
                        server.getAuthConfig(),
                        new TypeReference<Map<String, String>>() {}
                );
                String apiKey = authConfig.get("api_key");
                String headerName = authConfig.getOrDefault("header_name", "X-API-Key");
                if (apiKey != null && !apiKey.isEmpty()) {
                    headers.set(headerName, apiKey);
                }
            } catch (Exception e) {
                log.error("解析认证配置失败", e);
            }
        }

        return headers;
    }

    /**
     * 健康检查
     */
    public boolean healthCheck(McpServer server) {
        try {
            // 尝试获取工具列表作为健康检查
            listTools(server);
            return true;
        } catch (Exception e) {
            log.warn("MCP Server健康检查失败: serverCode={}, error={}", server.getServerCode(), e.getMessage());
            return false;
        }
    }
}

