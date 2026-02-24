package com.aimanager.qa.controller;

import com.aimanager.common.result.Result;
import com.aimanager.qa.dto.*;
import com.aimanager.qa.service.McpService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * MCP管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    /**
     * 分页查询MCP Server列表
     */
    @GetMapping("/servers")
    public Result<Page<McpServerVO>> pageServers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        Page<McpServerVO> result = mcpService.pageServers(pageNum, pageSize, keyword, category);
        return Result.success(result);
    }

    /**
     * 获取MCP Server详情
     */
    @GetMapping("/servers/{id}")
    public Result<McpServerVO> getServerById(@PathVariable Long id) {
        McpServerVO server = mcpService.getServerById(id);
        return Result.success(server);
    }

    /**
     * 创建MCP Server
     */
    @PostMapping("/servers")
    public Result<Long> createServer(@RequestBody McpServerCreateRequest request) {
        Long id = mcpService.createServer(request);
        return Result.success(id);
    }

    /**
     * 更新MCP Server
     */
    @PutMapping("/servers/{id}")
    public Result<Void> updateServer(@PathVariable Long id, @RequestBody McpServerCreateRequest request) {
        mcpService.updateServer(id, request);
        return Result.success();
    }

    /**
     * 删除MCP Server
     */
    @DeleteMapping("/servers/{id}")
    public Result<Void> deleteServer(@PathVariable Long id) {
        mcpService.deleteServer(id);
        return Result.success();
    }

    /**
     * 启用/禁用MCP Server
     */
    @PutMapping("/servers/{id}/status")
    public Result<Void> toggleServerStatus(@PathVariable Long id, @RequestParam Integer status) {
        mcpService.toggleServerStatus(id, status);
        return Result.success();
    }

    /**
     * 发现/刷新MCP Server的工具列表
     */
    @PostMapping("/servers/{id}/discover-tools")
    public Result<List<McpToolVO>> discoverTools(@PathVariable Long id) {
        List<McpToolVO> tools = mcpService.discoverTools(id);
        return Result.success(tools);
    }

    /**
     * 启用/禁用MCP工具
     */
    @PutMapping("/tools/{toolId}/status")
    public Result<Void> toggleToolStatus(@PathVariable Long toolId, @RequestParam Integer enabled) {
        mcpService.toggleToolStatus(toolId, enabled);
        return Result.success();
    }

    /**
     * 调用MCP工具
     */
    @PostMapping("/call-tool")
    public Result<McpToolCallResponse> callTool(@RequestBody McpToolCallRequest request) {
        McpToolCallResponse response = mcpService.callTool(request);
        return Result.success(response);
    }

    /**
     * 获取所有可用的MCP工具（用于AI调用）
     */
    @GetMapping("/available-tools")
    public Result<List<Map<String, Object>>> getAvailableTools() {
        List<Map<String, Object>> tools = mcpService.getAvailableMcpTools();
        return Result.success(tools);
    }
}

