package com.aimanager.qa.service;

import com.aimanager.qa.entity.ExternalFunction;
import com.aimanager.qa.entity.FunctionCallHistory;
import com.aimanager.qa.mapper.ExternalFunctionMapper;
import com.aimanager.qa.mapper.FunctionCallHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Function Call 服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FunctionCallService {
    
    private final ExternalFunctionMapper functionMapper;
    private final FunctionCallHistoryMapper historyMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取所有启用的函数定义（用于传递给大模型）
     */
    public List<Map<String, Object>> getAvailableFunctions() {
        List<ExternalFunction> functions = functionMapper.selectList(
            new LambdaQueryWrapper<ExternalFunction>()
                .eq(ExternalFunction::getStatus, 1)
                .eq(ExternalFunction::getDeleted, 0)
        );
        
        return functions.stream().map(this::convertToFunctionDefinition).collect(Collectors.toList());
    }
    
    /**
     * 将函数实体转换为大模型可识别的函数定义格式
     */
    private Map<String, Object> convertToFunctionDefinition(ExternalFunction function) {
        Map<String, Object> definition = new HashMap<>();
        definition.put("name", function.getFunctionName());
        definition.put("description", function.getDescription());
        
        try {
            // 解析参数 Schema
            if (function.getParametersSchema() != null && !function.getParametersSchema().isEmpty()) {
                JsonNode schemaNode = objectMapper.readTree(function.getParametersSchema());
                definition.put("parameters", objectMapper.convertValue(schemaNode, Map.class));
            }
        } catch (Exception e) {
            log.error("解析函数参数Schema失败: functionName={}, error={}", 
                function.getFunctionName(), e.getMessage());
        }
        
        return definition;
    }
    
    /**
     * 执行函数调用
     * 
     * @param functionName 函数名称
     * @param arguments 参数（JSON字符串）
     * @param sessionId 会话ID
     * @return 函数执行结果
     */
    public String executeFunction(String functionName, String arguments, String sessionId) {
        long startTime = System.currentTimeMillis();
        
        log.info("开始执行函数: functionName={}, arguments={}, sessionId={}", 
            functionName, arguments, sessionId);
        
        // 查询函数定义
        ExternalFunction function = functionMapper.selectOne(
            new LambdaQueryWrapper<ExternalFunction>()
                .eq(ExternalFunction::getFunctionName, functionName)
                .eq(ExternalFunction::getStatus, 1)
                .eq(ExternalFunction::getDeleted, 0)
        );
        
        if (function == null) {
            log.error("函数不存在或未启用: {}", functionName);
            return createErrorResult("函数不存在或未启用");
        }
        
        FunctionCallHistory history = new FunctionCallHistory();
        history.setSessionId(sessionId);
        history.setFunctionId(function.getId());
        history.setFunctionName(functionName);
        history.setInputParams(arguments);
        history.setCreateTime(LocalDateTime.now());
        
        try {
            // 解析参数
            Map<String, Object> params = parseArguments(arguments);
            
            // 构建请求
            String url = buildUrl(function.getApiUrl(), params, function.getHttpMethod());
            HttpHeaders headers = buildHeaders(function);
            HttpEntity<?> entity = buildRequestEntity(params, headers, function.getHttpMethod());
            
            log.info("调用外部API: url={}, method={}", url, function.getHttpMethod());
            
            // 执行HTTP请求
            ResponseEntity<String> response;
            if ("GET".equalsIgnoreCase(function.getHttpMethod())) {
                response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            } else if ("POST".equalsIgnoreCase(function.getHttpMethod())) {
                response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            } else {
                throw new RuntimeException("不支持的HTTP方法: " + function.getHttpMethod());
            }
            
            String responseBody = response.getBody();
            log.info("外部API响应: {}", responseBody);
            
            // 映射响应
            String result = mapResponse(responseBody, function.getResponseMapping());
            
            // 记录成功
            history.setOutputResult(result);
            history.setStatus("SUCCESS");
            history.setResponseTime(System.currentTimeMillis() - startTime);
            historyMapper.insert(history);
            
            log.info("函数执行成功: functionName={}, result={}", functionName, result);
            return result;
            
        } catch (Exception e) {
            log.error("函数执行失败: functionName={}, error={}", functionName, e.getMessage(), e);
            
            // 记录失败
            history.setStatus("FAILED");
            history.setErrorMessage(e.getMessage());
            history.setResponseTime(System.currentTimeMillis() - startTime);
            historyMapper.insert(history);
            
            return createErrorResult(e.getMessage());
        }
    }
    
    /**
     * 解析参数
     */
    private Map<String, Object> parseArguments(String arguments) throws Exception {
        if (arguments == null || arguments.trim().isEmpty()) {
            return new HashMap<>();
        }
        return objectMapper.readValue(arguments, Map.class);
    }

    /**
     * 构建URL（对于GET请求，将参数拼接到URL）
     */
    private String buildUrl(String baseUrl, Map<String, Object> params, String httpMethod) {
        if (!"GET".equalsIgnoreCase(httpMethod) || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder url = new StringBuilder(baseUrl);
        if (!baseUrl.contains("?")) {
            url.append("?");
        } else if (!baseUrl.endsWith("&")) {
            url.append("&");
        }

        params.forEach((key, value) -> {
            url.append(key).append("=").append(value).append("&");
        });

        // 移除最后一个 &
        if (url.charAt(url.length() - 1) == '&') {
            url.setLength(url.length() - 1);
        }

        return url.toString();
    }

    /**
     * 构建请求头
     */
    private HttpHeaders buildHeaders(ExternalFunction function) throws Exception {
        HttpHeaders headers = new HttpHeaders();

        // 解析自定义请求头
        if (function.getHeaders() != null && !function.getHeaders().isEmpty()) {
            Map<String, String> customHeaders = objectMapper.readValue(function.getHeaders(), Map.class);
            customHeaders.forEach(headers::set);
        }

        // 添加认证信息
        if (function.getAuthType() != null && !"NONE".equalsIgnoreCase(function.getAuthType())) {
            addAuthentication(headers, function);
        }

        return headers;
    }

    /**
     * 添加认证信息
     */
    private void addAuthentication(HttpHeaders headers, ExternalFunction function) throws Exception {
        String authType = function.getAuthType();
        String authConfig = function.getAuthConfig();

        if (authConfig == null || authConfig.isEmpty()) {
            return;
        }

        Map<String, String> config = objectMapper.readValue(authConfig, Map.class);

        switch (authType.toUpperCase()) {
            case "BEARER":
                String token = config.get("token");
                if (token != null) {
                    headers.set("Authorization", "Bearer " + token);
                }
                break;
            case "API_KEY":
                String apiKey = config.get("api_key");
                String headerName = config.getOrDefault("header_name", "X-API-Key");
                if (apiKey != null) {
                    headers.set(headerName, apiKey);
                }
                break;
            case "BASIC":
                String username = config.get("username");
                String password = config.get("password");
                if (username != null && password != null) {
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.set("Authorization", "Basic " + encodedAuth);
                }
                break;
        }
    }

    /**
     * 构建请求实体
     */
    private HttpEntity<?> buildRequestEntity(Map<String, Object> params, HttpHeaders headers, String httpMethod) {
        if ("GET".equalsIgnoreCase(httpMethod)) {
            return new HttpEntity<>(headers);
        } else {
            return new HttpEntity<>(params, headers);
        }
    }

    /**
     * 映射响应（使用JSONPath提取需要的字段）
     */
    private String mapResponse(String responseBody, String responseMapping) throws Exception {
        if (responseMapping == null || responseMapping.isEmpty()) {
            return responseBody;
        }

        Map<String, String> mapping = objectMapper.readValue(responseMapping, Map.class);
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            String fieldName = entry.getKey();
            String jsonPath = entry.getValue();

            try {
                Object value = JsonPath.read(responseBody, jsonPath);
                result.put(fieldName, value);
            } catch (Exception e) {
                log.warn("JSONPath提取失败: path={}, error={}", jsonPath, e.getMessage());
                result.put(fieldName, null);
            }
        }

        return objectMapper.writeValueAsString(result);
    }

    /**
     * 创建错误结果
     */
    private String createErrorResult(String errorMessage) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", errorMessage);
        error.put("success", false);

        try {
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            return "{\"error\": \"" + errorMessage + "\", \"success\": false}";
        }
    }
}
