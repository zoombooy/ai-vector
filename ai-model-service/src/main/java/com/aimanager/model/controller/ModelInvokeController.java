package com.aimanager.model.controller;

import com.aimanager.common.result.Result;
import com.aimanager.model.dto.ChatRequest;
import com.aimanager.model.dto.ChatResponse;
import com.aimanager.model.dto.EmbeddingRequest;
import com.aimanager.model.dto.EmbeddingResponse;
import com.aimanager.model.service.ModelInvokeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 模型调用控制器
 */
@RestController
@RequestMapping("/model/invoke")
@RequiredArgsConstructor
public class ModelInvokeController {

    private final ModelInvokeService modelInvokeService;

    /**
     * 调用模型（非流式）
     */
    @PostMapping("/chat")
    public Result<ChatResponse> chat(@Validated @RequestBody ChatRequest request) {
        ChatResponse response = modelInvokeService.invoke(request);
        return Result.success(response);
    }

    /**
     * 调用模型（流式）
     */
    @PostMapping("/chat/stream")
    public SseEmitter chatStream(@Validated @RequestBody ChatRequest request) {
        return modelInvokeService.invokeStream(request);
    }

    /**
     * 测试模型连接
     */
    @GetMapping("/test/{modelId}")
    public Result<ChatResponse> testModel(@PathVariable("modelId") Long modelId) {
        ChatRequest request = new ChatRequest();
        request.setModelId(modelId);
        request.setMessages(java.util.List.of(
                new com.aimanager.model.dto.ChatMessage("system", "You are a helpful assistant."),
                new com.aimanager.model.dto.ChatMessage("user", "Hello")
        ));
        request.setStream(false);

        ChatResponse response = modelInvokeService.invoke(request);
        return Result.success(response);
    }

    /**
     * 文本向量化（Embedding）
     */
    @PostMapping("/embedding")
    public Result<EmbeddingResponse> embedding(@Validated @RequestBody EmbeddingRequest request) {
        EmbeddingResponse response = modelInvokeService.embedding(request);
        return Result.success(response);
    }
}

