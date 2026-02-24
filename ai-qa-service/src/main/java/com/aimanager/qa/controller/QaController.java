package com.aimanager.qa.controller;

import com.aimanager.common.result.Result;
import com.aimanager.qa.dto.QaHistoryVO;
import com.aimanager.qa.dto.QaRequest;
import com.aimanager.qa.dto.QaResponse;
import com.aimanager.qa.service.QaService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * AI问答控制器
 */
@RestController
@RequestMapping("/qa")
@RequiredArgsConstructor
public class QaController {

    private final QaService qaService;

    /**
     * 提问（非流式）
     */
    @PostMapping("/ask")
    public Result<QaResponse> ask(@Validated @RequestBody QaRequest request) {
        QaResponse response = qaService.ask(request);
        return Result.success(response);
    }

    /**
     * 提问（流式）- POST
     */
    @PostMapping("/ask/stream")
    public SseEmitter askStream(@Validated @RequestBody QaRequest request) {
        return qaService.askStream(request);
    }

    /**
     * 提问（流式）- GET
     */
    @GetMapping("/ask/stream")
    public SseEmitter askStreamGet(
            @RequestParam String question,
            @RequestParam Long modelId,
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "false") Boolean enableAgent,
            @RequestParam(defaultValue = "true") Boolean enableFunctionCall) {
        QaRequest request = new QaRequest();
        request.setQuestion(question);
        request.setModelId(modelId);
        request.setSessionId(sessionId);
        request.setEnableAgent(enableAgent);
        request.setEnableFunctionCall(enableFunctionCall);
        return qaService.askStream(request);
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public Result<List<QaHistoryVO>> getSessionHistory(@PathVariable("sessionId") String sessionId) {
        List<QaHistoryVO> history = qaService.getSessionHistory(sessionId);
        return Result.success(history);
    }

    /**
     * 获取用户的所有会话
     */
    @GetMapping("/sessions")
    public Result<List<String>> getUserSessions(@RequestParam(value = "userId", required = false) Long userId) {
        List<String> sessions = qaService.getUserSessions(userId);
        return Result.success(sessions);
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/session/{sessionId}")
    public Result<Void> deleteSession(@PathVariable("sessionId") String sessionId) {
        qaService.deleteSession(sessionId);
        return Result.success();
    }
}

