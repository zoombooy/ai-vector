package com.aimanager.vector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Embedding 向量化服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${embedding.model-url:http://localhost:8084/model/invoke/embedding}")
    private String modelUrl;
    
    @Value("${embedding.model-id:4}")
    private Long modelId;
    
    /**
     * 将文本转换为向量
     * 
     * @param text 文本内容
     * @return 向量数组
     */
    public List<Float> textToVector(String text) {
        try {
            log.debug("开始向量化文本，长度: {}", text.length());

            // 构建请求 - 使用配置的modelId
            Map<String, Object> request = new HashMap<>();
            request.put("modelId", modelId);
            request.put("input", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 将请求体序列化为 JSON 字符串
            String requestJson = objectMapper.writeValueAsString(request);
            log.debug("调用 Embedding API: url={}, modelId={}", modelUrl, modelId);

            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);

            // 调用 embedding 模型
            ResponseEntity<String> response;
            try {
                response = restTemplate.postForEntity(
                        modelUrl,
                        entity,
                        String.class
                );
            } catch (Exception e) {
                log.error("调用模型服务失败: url={}, error={}", modelUrl, e.getMessage());
                throw new RuntimeException("模型服务连接失败，请确保ai-model-service已启动: " + e.getMessage());
            }

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            int code = jsonNode.path("code").asInt();

            if (code != 200) {
                String errorMsg = jsonNode.path("message").asText("向量化失败");
                log.error("向量化API返回错误: code={}, message={}", code, errorMsg);
                throw new RuntimeException("向量化失败(code=" + code + "): " + errorMsg);
            }

            // 提取向量数据
            JsonNode dataNode = jsonNode.path("data");
            JsonNode embeddingNode = dataNode.path("embedding");

            if (embeddingNode.isMissingNode() || embeddingNode.isNull()) {
                log.error("响应中没有embedding数据: {}", response.getBody());
                throw new RuntimeException("向量化响应格式错误，缺少embedding数据");
            }

            List<Float> vector = objectMapper.convertValue(
                    embeddingNode,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Float.class)
            );

            log.debug("向量化成功，维度: {}", vector.size());
            return vector;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("向量化失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量化失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量向量化
     * 
     * @param texts 文本列表
     * @return 向量列表
     */
    public List<List<Float>> batchTextToVector(List<String> texts) {
        return texts.stream()
                .map(this::textToVector)
                .toList();
    }
}

