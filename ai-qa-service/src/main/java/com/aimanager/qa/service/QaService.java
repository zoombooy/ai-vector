package com.aimanager.qa.service;

import com.aimanager.qa.dto.*;
import com.aimanager.qa.entity.ExternalAgent;
import com.aimanager.qa.entity.QaHistory;
import com.aimanager.qa.mapper.QaHistoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI问答服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QaService {

    private final QaHistoryMapper qaHistoryMapper;
    private final FunctionCallService functionCallService;
    private final AgentService agentService;
    private final McpService mcpService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ai-model-service 的地址
    private static final String MODEL_SERVICE_URL = "http://localhost:8084";

    @Value("${vector.service.url}")
    private String vectorServiceUrl;

    @Value("${knowledge.service.url}")
    private String knowledgeServiceUrl;

    @Value("${function.call.enabled:true}")
    private boolean functionCallEnabled;

    @Value("${function.call.max.iterations:3}")
    private int maxFunctionCallIterations;

    public static void main(){

    }

    /**
     * 处理问答请求（非流式）
     */
    public QaResponse ask(QaRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("收到问答请求: question={}, sessionId={}, modelId={}",
                request.getQuestion(), request.getSessionId(), request.getModelId());

        // 生成或使用现有会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        try {
            // 获取会话历史（用于上下文）
            List<QaHistory> history = getHistoryBySessionId(sessionId);

            // 检索相关文档（RAG）- 使用分块检索获取更精确的内容
            String documentContext = searchAndGetChunkContext(request.getQuestion());
            // 如果分块检索失败，回退到文档级检索
            if (documentContext.isEmpty()) {
                List<Long> relatedDocIds = searchRelatedDocuments(request.getQuestion());
                documentContext = getDocumentContext(relatedDocIds);
            }

            // 构建消息列表（包含文档上下文）
            List<ChatMessage> messages = buildMessages(request.getQuestion(), history, documentContext);

            // 获取可用的函数和Agent列表
            List<java.util.Map<String, Object>> functions = new ArrayList<>();
            List<String> calledFunctions = new ArrayList<>();
            List<String> calledAgents = new ArrayList<>();
            List<String> calledMcpTools = new ArrayList<>();

            if (request.getEnableFunctionCall() != null && request.getEnableFunctionCall()) {
                List<java.util.Map<String, Object>> availableFunctions = functionCallService.getAvailableFunctions();
                functions.addAll(availableFunctions);
                log.info("启用 Function Call，可用函数数量: {}", availableFunctions.size());
            }

            if (request.getEnableAgent() != null && request.getEnableAgent()) {
                List<java.util.Map<String, Object>> availableAgents = agentService.getAvailableAgents();
                functions.addAll(availableAgents);
                log.info("启用 Agent调用，可用Agent数量: {}", availableAgents.size());
            }

            if (request.getEnableMcp() != null && request.getEnableMcp()) {
                List<java.util.Map<String, Object>> availableMcpTools = mcpService.getAvailableMcpTools();
                functions.addAll(availableMcpTools);
                log.info("启用 MCP工具调用，可用MCP工具数量: {}", availableMcpTools.size());
            }

            // 调用模型服务（可能需要多次迭代处理 Function Call）
            ModelChatResponse chatResponse = callModelWithFunctionCall(
                request.getModelId(),
                messages,
                functions.isEmpty() ? null : functions,
                sessionId,
                calledFunctions,
                calledAgents,
                calledMcpTools
            );

            if (chatResponse == null) {
                log.error("chatResponse为null");
                throw new RuntimeException("模型调用失败：chatResponse为null");
            }

            if (chatResponse.getContent() == null) {
                log.error("chatResponse.content为null, chatResponse={}", chatResponse);
                throw new RuntimeException("模型调用失败：content为null");
            }

            log.info("解析后的响应: content={}, model={}, tokens={}",
                    chatResponse.getContent(), chatResponse.getModel(), chatResponse.getTotalTokens());

            // 保存问答历史
            QaHistory qaHistory = new QaHistory();
            qaHistory.setSessionId(sessionId);
            qaHistory.setQuestion(request.getQuestion());
            qaHistory.setAnswer(chatResponse.getContent());
            qaHistory.setModelId(request.getModelId());
            qaHistory.setModelName(chatResponse.getModel());
            qaHistory.setResponseTime(chatResponse.getResponseTime());
            qaHistory.setTotalTokens(chatResponse.getTotalTokens());
            qaHistoryMapper.insert(qaHistory);

            // 构建响应
            QaResponse qaResponse = new QaResponse();
            qaResponse.setAnswer(chatResponse.getContent());
            qaResponse.setSessionId(sessionId);
            qaResponse.setRelatedDocuments(List.of()); // 分块检索不直接返回文档ID
            qaResponse.setConfidence(documentContext.isEmpty() ? 0.5 : 1.0);
            qaResponse.setResponseTime(System.currentTimeMillis() - startTime);
            qaResponse.setCalledFunctions(calledFunctions);
            qaResponse.setCalledAgents(calledAgents);
            qaResponse.setCalledMcpTools(calledMcpTools);

            log.info("问答处理完成: sessionId={}, answer={}, responseTime={}ms",
                    sessionId, qaResponse.getAnswer(), qaResponse.getResponseTime());

            return qaResponse;

        } catch (Exception e) {
            log.error("问答处理失败: {}", e.getMessage(), e);
            throw new RuntimeException("问答处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理问答请求（流式）
     */
    public SseEmitter askStream(QaRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);

        // 生成或使用现有会话ID
        String sessionId = request.getSessionId();
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }
        final String finalSessionId = sessionId;

        // 异步处理
        new Thread(() -> {
            StringBuilder fullAnswer = new StringBuilder();
            long startTime = System.currentTimeMillis();

            try {
                // 发送会话ID
                emitter.send(SseEmitter.event().name("session").data(finalSessionId));

                // 获取会话历史
                List<QaHistory> history = getHistoryBySessionId(finalSessionId);

                // 检索相关文档（RAG）
                List<Long> relatedDocIds = searchRelatedDocuments(request.getQuestion());
                String documentContext = getDocumentContext(relatedDocIds);

                // 构建消息列表（包含文档上下文）
                List<ChatMessage> messages = buildMessages(request.getQuestion(), history, documentContext);

                // 获取可用的函数和Agent列表
                List<java.util.Map<String, Object>> functions = new ArrayList<>();
                List<String> calledFunctions = new ArrayList<>();
                List<String> calledAgents = new ArrayList<>();
                List<String> calledMcpTools = new ArrayList<>();

                if (request.getEnableFunctionCall() != null && request.getEnableFunctionCall()) {
                    List<java.util.Map<String, Object>> availableFunctions = functionCallService.getAvailableFunctions();
                    functions.addAll(availableFunctions);
                    log.info("流式接口启用 Function Call，可用函数数量: {}", availableFunctions.size());
                }

                if (request.getEnableAgent() != null && request.getEnableAgent()) {
                    List<java.util.Map<String, Object>> availableAgents = agentService.getAvailableAgents();
                    functions.addAll(availableAgents);
                    log.info("流式接口启用 Agent调用，可用Agent数量: {}", availableAgents.size());
                }

                if (request.getEnableMcp() != null && request.getEnableMcp()) {
                    List<java.util.Map<String, Object>> availableMcpTools = mcpService.getAvailableMcpTools();
                    functions.addAll(availableMcpTools);
                    log.info("流式接口启用 MCP工具调用，可用MCP工具数量: {}", availableMcpTools.size());
                }

                // 构建请求
                ModelChatRequest chatRequest = new ModelChatRequest();
                chatRequest.setModelId(request.getModelId());
                chatRequest.setMessages(messages);
                chatRequest.setStream(true);
                chatRequest.setFunctions(functions.isEmpty() ? null : functions);

                // 调用流式接口
                URL url = new URL(MODEL_SERVICE_URL + "/model/invoke/chat/stream");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // 发送请求
                String requestJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(chatRequest);
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
                        fullAnswer.append(data);
                        emitter.send(SseEmitter.event().name("message").data(data));
                    }
                }

                reader.close();

                // 保存问答历史
                QaHistory qaHistory = new QaHistory();
                qaHistory.setSessionId(finalSessionId);
                qaHistory.setQuestion(request.getQuestion());
                qaHistory.setAnswer(fullAnswer.toString());
                qaHistory.setModelId(request.getModelId());
                qaHistory.setResponseTime(System.currentTimeMillis() - startTime);
                qaHistoryMapper.insert(qaHistory);

                // 发送调用的Agent、Function和MCP工具信息
                if (!calledAgents.isEmpty()) {
                    emitter.send(SseEmitter.event().name("agents").data(objectMapper.writeValueAsString(calledAgents)));
                }
                if (!calledFunctions.isEmpty()) {
                    emitter.send(SseEmitter.event().name("functions").data(objectMapper.writeValueAsString(calledFunctions)));
                }
                if (!calledMcpTools.isEmpty()) {
                    emitter.send(SseEmitter.event().name("mcpTools").data(objectMapper.writeValueAsString(calledMcpTools)));
                }

                emitter.send(SseEmitter.event().name("done").data(""));
                emitter.complete();
                log.info("流式问答完成: sessionId={}", finalSessionId);

            } catch (Exception e) {
                log.error("流式问答失败: {}", e.getMessage(), e);
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }

    /**
     * 获取会话历史
     */
    public List<QaHistoryVO> getSessionHistory(String sessionId) {
        log.info("获取会话历史: sessionId={}", sessionId);

        List<QaHistory> historyList = qaHistoryMapper.selectList(
                new LambdaQueryWrapper<QaHistory>()
                        .eq(QaHistory::getSessionId, sessionId)
                        .orderByAsc(QaHistory::getCreateTime)
        );

        return historyList.stream().map(history -> {
            QaHistoryVO vo = new QaHistoryVO();
            BeanUtils.copyProperties(history, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取用户的所有会话
     */
    public List<String> getUserSessions(Long userId) {
        List<QaHistory> historyList = qaHistoryMapper.selectList(
                new LambdaQueryWrapper<QaHistory>()
                        .eq(userId != null, QaHistory::getUserId, userId)
                        .groupBy(QaHistory::getSessionId)
                        .orderByDesc(QaHistory::getCreateTime)
        );

        return historyList.stream()
                .map(QaHistory::getSessionId)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 删除会话历史
     */
    public void deleteSession(String sessionId) {
        qaHistoryMapper.delete(
                new LambdaQueryWrapper<QaHistory>().eq(QaHistory::getSessionId, sessionId)
        );
        log.info("删除会话历史: sessionId={}", sessionId);
    }

    /**
     * 获取会话历史（内部使用）
     */
    private List<QaHistory> getHistoryBySessionId(String sessionId) {
        return qaHistoryMapper.selectList(
                new LambdaQueryWrapper<QaHistory>()
                        .eq(QaHistory::getSessionId, sessionId)
                        .orderByAsc(QaHistory::getCreateTime)
                        .last("LIMIT 10") // 只取最近10条
        );
    }

    /**
     * 构建消息列表（包含历史上下文和文档上下文）
     */
    private List<ChatMessage> buildMessages(String question, List<QaHistory> history, String documentContext) {
        List<ChatMessage> messages = new ArrayList<>();

        // 系统提示词（包含文档上下文）
        String systemPrompt = "You are a helpful AI assistant. Please answer questions based on the context provided.";
        if (documentContext != null && !documentContext.isEmpty()) {
            systemPrompt += "\n\n参考文档内容：\n" + documentContext;
            systemPrompt += "\n\n请仔细阅读以上所有文档的内容，综合所有文档的信息来回答用户的问题。";
            systemPrompt += "如果多个文档都包含相关信息，请整合所有文档的内容，给出全面的答案。";
            systemPrompt += "如果文档中没有相关信息，请明确说明无法从提供的文档中找到答案。";
            systemPrompt += "在回答时，如果引用了某个文档的内容，可以注明文档标题。";
        }
        messages.add(new ChatMessage("user", systemPrompt));

        // 添加历史对话（最多5轮，且必须有完整的问答对）
        int historyCount = Math.min(history.size(), 5);
        for (int i = history.size() - historyCount; i < history.size(); i++) {
            QaHistory h = history.get(i);
            // 只添加有完整答案的历史记录
            if (h.getQuestion() != null && h.getAnswer() != null && !h.getAnswer().isEmpty()) {
                messages.add(new ChatMessage("user", h.getQuestion()));
                messages.add(new ChatMessage("assistant", h.getAnswer()));
            }
        }

        // 当前问题
        messages.add(new ChatMessage("user", question));

        log.info("构建消息列表: 历史记录{}条, 文档上下文长度={}, 总消息{}条",
                historyCount, documentContext != null ? documentContext.length() : 0, messages.size());
        return messages;
    }

    /**
     * 搜索相关文档（向量检索）- 返回文档ID列表（向后兼容）
     */
    private List<Long> searchRelatedDocuments(String queryText) {
        try {
            log.info("开始向量检索: queryText={}", queryText);

            // 增加 topK 到 10，以便检索更多相关文档
            String url = vectorServiceUrl + "/vector/search?queryText=" +
                    java.net.URLEncoder.encode(queryText, StandardCharsets.UTF_8) + "&topK=10";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                int code = jsonNode.path("code").asInt();

                if (code == 200) {
                    JsonNode dataNode = jsonNode.path("data");
                    List<Long> docIds = new ArrayList<>();
                    if (dataNode.isArray()) {
                        for (JsonNode node : dataNode) {
                            docIds.add(node.asLong());
                        }
                    }
                    log.info("向量检索成功: 找到{}个相关文档, 文档ID列表={}", docIds.size(), docIds);
                    return docIds;
                }
            }

            log.warn("向量检索失败: status={}", response.getStatusCode());
            return List.of();

        } catch (Exception e) {
            log.error("向量检索异常: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * 获取文档内容（基于分块检索，返回最相关的分块内容）
     */
    private String getDocumentContext(List<Long> documentIds) {
        // 由于调用方式不变，但我们现在改用分块检索
        // 这里需要重新实现
        if (documentIds == null || documentIds.isEmpty()) {
            log.info("文档ID列表为空，跳过获取文档上下文");
            return "";
        }

        log.info("开始获取文档上下文: 文档ID列表={}", documentIds);
        StringBuilder context = new StringBuilder();
        int successCount = 0;

        for (Long docId : documentIds) {
            try {
                String url = knowledgeServiceUrl + "/document/" + docId;
                log.debug("请求文档内容: docId={}, url={}", docId, url);

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    JsonNode jsonNode = objectMapper.readTree(response.getBody());
                    int code = jsonNode.path("code").asInt();

                    if (code == 200) {
                        JsonNode dataNode = jsonNode.path("data");
                        String title = dataNode.path("docTitle").asText("");
                        String content = dataNode.path("content").asText("");

                        if (!content.isEmpty()) {
                            context.append("\n\n【文档：").append(title).append("】\n");
                            // 限制每个文档的内容长度，避免超过模型上下文限制
                            int contentLength = content.length();
                            if (contentLength > 4000) {
                                context.append(content, 0, 4000).append("...");
                                log.info("  添加文档: docId={}, 标题={}, 内容长度={} (截断到4000)",
                                    docId, title, contentLength);
                            } else {
                                context.append(content);
                                log.info("  添加文档: docId={}, 标题={}, 内容长度={}",
                                    docId, title, contentLength);
                            }
                            successCount++;
                        } else {
                            log.warn("  文档内容为空: docId={}, 标题={}", docId, title);
                        }
                    } else {
                        log.warn("  获取文档失败: docId={}, code={}", docId, code);
                    }
                } else {
                    log.warn("  HTTP请求失败: docId={}, status={}", docId, response.getStatusCode());
                }
            } catch (Exception e) {
                log.error("获取文档内容失败: docId={}, error={}", docId, e.getMessage());
            }
        }

        log.info("获取文档上下文完成: 请求{}个文档, 成功{}个, 总长度={}",
            documentIds.size(), successCount, context.length());
        return context.toString();
    }

    /**
     * 基于分块的向量检索（新方法，返回更精确的相关内容）
     */
    private String searchAndGetChunkContext(String queryText) {
        try {
            log.info("===== 开始分块向量检索 =====");
            log.info("查询文本: {}", queryText);
            log.info("向量服务地址: {}", vectorServiceUrl);

            // 1. 调用分块检索接口
            String searchUrl = vectorServiceUrl + "/vector/search/chunks?queryText=" +
                    java.net.URLEncoder.encode(queryText, StandardCharsets.UTF_8) + "&topK=15";
            log.info("分块检索URL: {}", searchUrl);

            ResponseEntity<String> searchResponse = restTemplate.getForEntity(searchUrl, String.class);
            log.info("分块检索响应状态: {}", searchResponse.getStatusCode());

            if (searchResponse.getStatusCode() != HttpStatus.OK) {
                log.warn("分块向量检索失败: status={}", searchResponse.getStatusCode());
                return "";
            }

            log.debug("分块检索响应体: {}", searchResponse.getBody());
            JsonNode searchJson = objectMapper.readTree(searchResponse.getBody());
            int responseCode = searchJson.path("code").asInt();
            log.info("分块检索响应code: {}", responseCode);

            if (responseCode != 200) {
                log.warn("分块向量检索返回错误: code={}, message={}", responseCode, searchJson.path("message").asText());
                return "";
            }

            JsonNode chunksData = searchJson.path("data");
            log.info("分块检索返回数据类型: isArray={}, size={}", chunksData.isArray(), chunksData.size());

            if (!chunksData.isArray() || chunksData.isEmpty()) {
                log.info("分块向量检索未找到结果");
                return "";
            }

            // 2. 批量获取分块内容
            List<java.util.Map<String, Object>> chunkInfos = new ArrayList<>();
            for (JsonNode chunk : chunksData) {
                java.util.Map<String, Object> info = new java.util.HashMap<>();
                Long docId = chunk.path("docId").asLong();
                Integer chunkIndex = chunk.path("chunkIndex").asInt();
                Double score = chunk.path("score").asDouble();

                info.put("docId", docId);
                info.put("chunkIndex", chunkIndex);
                info.put("score", score);
                chunkInfos.add(info);

                log.info("  检索到分块: docId={}, chunkIndex={}, score={}", docId, chunkIndex, score);
            }

            String chunksUrl = knowledgeServiceUrl + "/document/chunks";
            log.info("获取分块内容URL: {}", chunksUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(chunkInfos), headers);

            ResponseEntity<String> chunksResponse = restTemplate.postForEntity(chunksUrl, entity, String.class);

            if (chunksResponse.getStatusCode() != HttpStatus.OK) {
                log.warn("获取分块内容失败: status={}", chunksResponse.getStatusCode());
                return "";
            }

            JsonNode chunksJson = objectMapper.readTree(chunksResponse.getBody());
            if (chunksJson.path("code").asInt() != 200) {
                log.warn("获取分块内容返回错误: {}", chunksJson.path("message").asText());
                return "";
            }

            // 3. 组装上下文
            JsonNode chunksArray = chunksJson.path("data");
            StringBuilder context = new StringBuilder();
            String lastDocTitle = "";

            for (JsonNode chunkNode : chunksArray) {
                String docTitle = chunkNode.path("docTitle").asText("");
                String chunkContent = chunkNode.path("chunkContent").asText("");
                int chunkIndex = chunkNode.path("chunkIndex").asInt();

                if (!chunkContent.isEmpty()) {
                    // 如果是新文档，添加文档标题
                    if (!docTitle.equals(lastDocTitle)) {
                        context.append("\n\n【文档：").append(docTitle).append("】\n");
                        lastDocTitle = docTitle;
                    }
                    context.append("\n[片段").append(chunkIndex + 1).append("]: ");
                    context.append(chunkContent);
                }
            }

            log.info("分块向量检索完成: 找到{}个相关分块, 上下文长度={}",
                chunksArray.size(), context.length());
            return context.toString();

        } catch (Exception e) {
            log.error("分块向量检索异常: {}", e.getMessage(), e);
            return "";
        }
    }

    /**
     * 调用模型（支持 Function Call 迭代）
     */
    private ModelChatResponse callModelWithFunctionCall(
            Long modelId,
            List<ChatMessage> messages,
            List<java.util.Map<String, Object>> functions,
            String sessionId,
            List<String> calledFunctions,
            List<String> calledAgents,
            List<String> calledMcpTools) throws Exception {

        int iteration = 0;
        List<ChatMessage> currentMessages = new ArrayList<>(messages);

        while (iteration < maxFunctionCallIterations) {
            iteration++;
            log.info("Function Call 迭代 {}/{}", iteration, maxFunctionCallIterations);

            // 构建请求
            ModelChatRequest chatRequest = new ModelChatRequest();
            chatRequest.setModelId(modelId);
            chatRequest.setMessages(currentMessages);
            chatRequest.setStream(false);
            chatRequest.setFunctions(functions);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<ModelChatRequest> entity = new HttpEntity<>(chatRequest, headers);

            // 调用模型服务
            ResponseEntity<String> response = restTemplate.postForEntity(
                    MODEL_SERVICE_URL + "/model/invoke/chat",
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            log.info("模型服务原始响应: {}", responseBody);

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            int code = jsonNode.path("code").asInt();

            if (code != 200) {
                String errorMsg = jsonNode.path("message").asText("模型调用失败");
                log.error("模型服务返回错误: code={}, message={}", code, errorMsg);
                throw new RuntimeException(errorMsg);
            }

            JsonNode dataNode = jsonNode.path("data");
            ModelChatResponse chatResponse = objectMapper.treeToValue(dataNode, ModelChatResponse.class);

            // 检查是否有 function_call
            if (chatResponse.getFunctionCall() != null) {
                String functionName = chatResponse.getFunctionCall().getName();
                String arguments = chatResponse.getFunctionCall().getArguments();

                log.info("模型请求调用函数/Agent/MCP: name={}, arguments={}", functionName, arguments);

                String functionResult;

                // 判断是MCP工具、Agent还是Function
                if (functionName.contains("__")) {
                    // MCP工具调用（格式：serverCode__toolName）
                    log.info("执行MCP工具: {}", functionName);
                    functionResult = mcpService.executeMcpToolByFunctionCall(functionName, arguments, sessionId);
                    calledMcpTools.add(functionName);
                } else {
                    ExternalAgent agent = agentService.getByCode(functionName);
                    if (agent != null) {
                        // 是Agent调用
                        log.info("执行Agent: {}", functionName);
                        functionResult = agentService.executeAgentByFunctionCall(functionName, arguments, sessionId);
                        calledAgents.add(functionName);
                    } else {
                        // 是Function调用
                        log.info("执行Function: {}", functionName);
                        functionResult = functionCallService.executeFunction(functionName, arguments, sessionId);
                        calledFunctions.add(functionName);
                    }
                }

                log.info("执行结果: {}", functionResult);

                // 将函数调用和结果添加到消息历史
                ChatMessage assistantMessage = new ChatMessage();
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(chatResponse.getContent());
                assistantMessage.setFunctionCall(chatResponse.getFunctionCall());
                currentMessages.add(assistantMessage);

                ChatMessage functionMessage = new ChatMessage();
                functionMessage.setRole("function");
                functionMessage.setName(functionName);
                functionMessage.setContent(functionResult);
                currentMessages.add(functionMessage);

                // 继续下一轮迭代，让模型基于函数结果生成最终答案
                continue;
            }

            // 没有 function_call，返回最终结果
            return chatResponse;
        }

        log.warn("达到最大 Function Call 迭代次数: {}", maxFunctionCallIterations);
        throw new RuntimeException("Function Call 迭代次数超限");
    }
}

