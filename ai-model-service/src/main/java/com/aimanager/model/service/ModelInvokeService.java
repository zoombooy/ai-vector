package com.aimanager.model.service;
import com.aimanager.model.dto.ChatRequest;
import com.aimanager.model.dto.ChatResponse;
import com.aimanager.model.dto.EmbeddingRequest;
import com.aimanager.model.dto.EmbeddingResponse;
import com.aimanager.model.entity.AiModel;
import com.aimanager.model.entity.ModelConfig;
import com.aimanager.model.mapper.AiModelMapper;
import com.aimanager.model.mapper.ModelConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型调用服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModelInvokeService {

    private final AiModelMapper aiModelMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 调用模型（非流式）
     */
    public ChatResponse invoke(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        // 获取模型和配置
        AiModel model = aiModelMapper.selectById(request.getModelId());
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        if (model.getStatus() != 1) {
            throw new RuntimeException("模型未启用");
        }

        ModelConfig config = modelConfigMapper.selectOne(
                new LambdaQueryWrapper<ModelConfig>().eq(ModelConfig::getModelId, model.getId())
        );
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }

        try {
            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(model, config, request, false);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 将请求体序列化为 JSON 字符串
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            log.debug("构建请求体: {}", requestBodyJson);

            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            // 调用API
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(),
                    entity,
                    String.class
            );

            // 解析响应
            String responseBody = response.getBody();
            log.debug("模型API响应: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 检查是否有错误
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                log.error("模型API返回错误: {}", errorMsg);
                throw new RuntimeException("模型API错误: " + errorMsg);
            }

            // 解析成功响应
            ChatResponse chatResponse = new ChatResponse();

            // 提取消息内容
            JsonNode choices = jsonNode.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");

                // 提取文本内容
                chatResponse.setContent(message.path("content").asText());
                chatResponse.setFinishReason(firstChoice.path("finish_reason").asText());

                // 检查是否有 function_call
                if (message.has("function_call")) {
                    JsonNode functionCallNode = message.path("function_call");
                    ChatResponse.FunctionCall functionCall = new ChatResponse.FunctionCall();
                    functionCall.setName(functionCallNode.path("name").asText());
                    functionCall.setArguments(functionCallNode.path("arguments").asText());
                    chatResponse.setFunctionCall(functionCall);

                    log.info("模型返回函数调用: name={}, arguments={}",
                        functionCall.getName(), functionCall.getArguments());
                }
            } else {
                throw new RuntimeException("响应中没有choices数据");
            }

            // 设置模型信息
            chatResponse.setModel(jsonNode.path("model").asText(model.getModelCode()));

            // 提取token使用情况
            if (jsonNode.has("usage")) {
                JsonNode usage = jsonNode.path("usage");
                chatResponse.setTotalTokens(usage.path("total_tokens").asInt());
                chatResponse.setPromptTokens(usage.path("prompt_tokens").asInt());
                chatResponse.setCompletionTokens(usage.path("completion_tokens").asInt());
            }

            chatResponse.setResponseTime(System.currentTimeMillis() - startTime);

            log.info("模型调用成功: modelId={}, model={}, tokens={}, responseTime={}ms",
                    model.getId(), chatResponse.getModel(), chatResponse.getTotalTokens(), chatResponse.getResponseTime());
            return chatResponse;

        } catch (Exception e) {
            log.error("模型调用失败: modelId={}, error={}", model.getId(), e.getMessage(), e);
            throw new RuntimeException("模型调用失败: " + e.getMessage());
        }
    }

    /**
     * 调用模型（流式）
     */
    public SseEmitter invokeStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 异步处理
        new Thread(() -> {
            try {
                // 获取模型和配置
                AiModel model = aiModelMapper.selectById(request.getModelId());
                if (model == null) {
                    emitter.completeWithError(new RuntimeException("模型不存在"));
                    return;
                }

                ModelConfig config = modelConfigMapper.selectOne(
                        new LambdaQueryWrapper<ModelConfig>().eq(ModelConfig::getModelId, model.getId())
                );
                if (config == null) {
                    emitter.completeWithError(new RuntimeException("模型配置不存在"));
                    return;
                }

                // 构建请求体
                Map<String, Object> requestBody = buildRequestBody(model, config, request, true);
                String requestJson = objectMapper.writeValueAsString(requestBody);

                // 创建HTTP连接
                URL url = new URL(config.getApiUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
                connection.setDoOutput(true);

                // 发送请求
                connection.getOutputStream().write(requestJson.getBytes(StandardCharsets.UTF_8));

                // 读取流式响应
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6);
                        if ("[DONE]".equals(data)) {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                            break;
                        }

                        try {
                            JsonNode jsonNode = objectMapper.readTree(data);
                            String content = jsonNode.path("choices").get(0).path("delta").path("content").asText();
                            if (!content.isEmpty()) {
                                emitter.send(SseEmitter.event().name("message").data(content));
                            }
                        } catch (Exception e) {
                            log.warn("解析流式响应失败: {}", e.getMessage());
                        }
                    }
                }

                reader.close();
                emitter.complete();
                log.info("流式调用完成: modelId={}", model.getId());

            } catch (Exception e) {
                log.error("流式调用失败: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 构建请求体（按照豆包 API 格式）
     */
    private Map<String, Object> buildRequestBody(AiModel model, ModelConfig config, ChatRequest request, boolean stream) {
        Map<String, Object> body = new HashMap<>();

        // 必需参数
        body.put("model", model.getModelCode());
        body.put("messages", request.getMessages());

        // 流式参数（只在流式调用时添加）
        if (stream) {
            body.put("stream", true);
        }

        // 可选参数 - temperature（0.0-2.0）
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        } else if (config.getTemperature() != null) {
            body.put("temperature", config.getTemperature());
        }

        // 可选参数 - max_tokens
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        } else if (config.getMaxTokens() != null) {
            body.put("max_tokens", config.getMaxTokens());
        }

        // 可选参数 - top_p（0.0-1.0）
        if (request.getTopP() != null) {
            body.put("top_p", request.getTopP());
        } else if (config.getTopP() != null) {
            body.put("top_p", config.getTopP());
        }

        // 可选参数 - frequency_penalty（-2.0-2.0）
        if (config.getFrequencyPenalty() != null) {
            body.put("frequency_penalty", config.getFrequencyPenalty());
        }

        // 可选参数 - presence_penalty（-2.0-2.0）
        if (config.getPresencePenalty() != null) {
            body.put("presence_penalty", config.getPresencePenalty());
        }

        // Function Call 参数
        if (request.getFunctions() != null && !request.getFunctions().isEmpty()) {
            body.put("functions", request.getFunctions());
            if (request.getFunctionCall() != null) {
                body.put("function_call", request.getFunctionCall());
            } else {
                body.put("function_call", "auto");
            }
        }

        log.info("构建请求体: {}", body);
        return body;
    }

    /**
     * 文本向量化（Embedding）
     */
    public EmbeddingResponse embedding(EmbeddingRequest request) {
        long startTime = System.currentTimeMillis();

        // 获取模型和配置
        AiModel model = aiModelMapper.selectById(request.getModelId());
        if (model == null) {
            throw new RuntimeException("模型不存在");
        }
        if (model.getStatus() != 1) {
            throw new RuntimeException("模型未启用");
        }

        ModelConfig config = modelConfigMapper.selectOne(
                new LambdaQueryWrapper<ModelConfig>().eq(ModelConfig::getModelId, model.getId())
        );
        if (config == null) {
            throw new RuntimeException("模型配置不存在");
        }

        try {
            // 构建请求体（兼容 OpenAI 和豆包 Embeddings API 格式）
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model.getModelCode());

            // 豆包支持数组格式，但我们只传单个文本，所以包装成数组
            List<String> inputList = new ArrayList<>();
            inputList.add(request.getInput());
            requestBody.put("input", inputList);

            // 豆包需要指定编码格式
            requestBody.put("encoding_format", "float");

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + config.getApiKey());

            // 将请求体序列化为 JSON 字符串
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            log.info("调用Embedding API: url={}, model={}, inputLength={}",
                    config.getApiUrl(), model.getModelCode(), request.getInput().length());
            log.info("请求体: {}", requestBodyJson);

            // 使用 JSON 字符串作为请求体（而不是 Map）
            HttpEntity<String> entity = new HttpEntity<>(requestBodyJson, headers);

            // 调用 Embedding API
            ResponseEntity<String> response = restTemplate.postForEntity(
                    config.getApiUrl(),
                    entity,
                    String.class
            );

            // 解析响应
            String responseBody = response.getBody();
            log.debug("Embedding API响应: {}", responseBody);

            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // 检查是否有错误
            if (jsonNode.has("error")) {
                String errorMsg = jsonNode.path("error").path("message").asText();
                String errorCode = jsonNode.path("error").path("code").asText();
                log.error("Embedding API返回错误: code={}, message={}", errorCode, errorMsg);
                throw new RuntimeException("Embedding API错误: " + errorMsg);
            }

            // 解析成功响应
            EmbeddingResponse embeddingResponse = new EmbeddingResponse();

            // 提取向量数据
            JsonNode dataNode = jsonNode.path("data");
            if (dataNode.isArray() && dataNode.size() > 0) {
                // 取第一个结果（因为我们只传了一个文本）
                JsonNode firstData = dataNode.get(0);
                JsonNode embeddingNode = firstData.path("embedding");

                if (embeddingNode.isArray()) {
                    List<Float> embedding = new ArrayList<>();
                    for (JsonNode node : embeddingNode) {
                        embedding.add((float) node.asDouble());
                    }
                    embeddingResponse.setEmbedding(embedding);
                    embeddingResponse.setDimension(embedding.size());
                } else {
                    throw new RuntimeException("响应中没有embedding数据");
                }
            } else {
                throw new RuntimeException("响应中没有data数据");
            }

            // 设置模型信息
            embeddingResponse.setModel(jsonNode.path("model").asText(model.getModelCode()));

            // 提取token使用情况
            if (jsonNode.has("usage")) {
                JsonNode usage = jsonNode.path("usage");
                embeddingResponse.setTotalTokens(usage.path("total_tokens").asInt());
            }

            embeddingResponse.setResponseTime(System.currentTimeMillis() - startTime);

            log.info("Embedding调用成功: modelId={}, model={}, dimension={}, tokens={}, responseTime={}ms",
                    model.getId(), model.getModelCode(), embeddingResponse.getDimension(),
                    embeddingResponse.getTotalTokens(), embeddingResponse.getResponseTime());
            return embeddingResponse;

        } catch (Exception e) {
            log.error("Embedding调用失败: modelId={}, error={}", model.getId(), e.getMessage(), e);
            throw new RuntimeException("Embedding调用失败: " + e.getMessage());
        }
    }
}

