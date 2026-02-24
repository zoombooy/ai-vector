package com.aimanager.qa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aimanager.common.result.Result;
import com.aimanager.qa.dto.AgentExecuteRequest;
import com.aimanager.qa.dto.AgentExecuteResponse;
import com.aimanager.qa.dto.AgentVO;
import com.aimanager.qa.entity.AgentExecutionHistory;
import com.aimanager.qa.entity.ExternalAgent;
import com.aimanager.qa.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Agent控制器
 */
@RestController
@RequestMapping("/agent")
@RequiredArgsConstructor
public class AgentController {
    
    private final AgentService agentService;
    
    /**
     * 分页查询Agent
     */
    @GetMapping("/page")
    public Result<Page<AgentVO>> pageAgents(
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {
        
        Page<AgentVO> page = agentService.pageAgents(pageNum, pageSize, keyword, category);
        return Result.success(page);
    }
    
    /**
     * 根据ID获取Agent
     */
    @GetMapping("/{id}")
    public Result<AgentVO> getAgentById(@PathVariable Long id) {
        AgentVO agent = agentService.getAgentById(id);
        return Result.success(agent);
    }
    
    /**
     * 根据编码获取Agent
     */
    @GetMapping("/code/{agentCode}")
    public Result<AgentVO> getAgentByCode(@PathVariable String agentCode) {
        AgentVO agent = agentService.getAgentByCode(agentCode);
        return Result.success(agent);
    }
    
    /**
     * 创建Agent
     */
    @PostMapping
    public Result<Long> createAgent(@Validated @RequestBody ExternalAgent agent) {
        Long id = agentService.createAgent(agent);
        return Result.success(id);
    }
    
    /**
     * 更新Agent
     */
    @PutMapping("/{id}")
    public Result<Void> updateAgent(@PathVariable Long id, @Validated @RequestBody ExternalAgent agent) {
        agent.setId(id);
        agentService.updateAgent(agent);
        return Result.success();
    }
    
    /**
     * 删除Agent
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAgent(@PathVariable Long id) {
        agentService.deleteAgent(id);
        return Result.success();
    }
    
    /**
     * 执行Agent
     */
    @PostMapping("/execute")
    public Result<AgentExecuteResponse> executeAgent(@Validated @RequestBody AgentExecuteRequest request) {
        AgentExecuteResponse response = agentService.executeAgent(request);
        return Result.success(response);
    }
    
    /**
     * 获取所有可用的Agent（用于AI调用）
     */
    @GetMapping("/available")
    public Result<List<Map<String, Object>>> getAvailableAgents() {
        List<Map<String, Object>> agents = agentService.getAvailableAgents();
        return Result.success(agents);
    }
    
    /**
     * 获取Agent执行历史
     */
    @GetMapping("/history")
    public Result<List<AgentExecutionHistory>> getExecutionHistory(
            @RequestParam(value = "sessionId", required = false) String sessionId,
            @RequestParam(value = "agentCode", required = false) String agentCode) {
        
        List<AgentExecutionHistory> history = agentService.getExecutionHistory(sessionId, agentCode);
        return Result.success(history);
    }
}

