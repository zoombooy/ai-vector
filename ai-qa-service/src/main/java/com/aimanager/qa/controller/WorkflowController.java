package com.aimanager.qa.controller;

import com.aimanager.qa.dto.*;
import com.aimanager.qa.entity.Workflow;
import com.aimanager.qa.service.WorkflowService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/workflow")
@RequiredArgsConstructor
public class WorkflowController {
    
    private final WorkflowService workflowService;
    
    /**
     * 分页查询工作流
     */
    @GetMapping("/page")
    public Map<String, Object> getWorkflowPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        
        Page<Workflow> page = workflowService.getWorkflowPage(pageNum, pageSize, keyword);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("pageNum", page.getCurrent());
        result.put("pageSize", page.getSize());
        
        return result;
    }
    
    /**
     * 根据ID获取工作流详情
     */
    @GetMapping("/{id}")
    public WorkflowDTO getWorkflowById(@PathVariable Long id) {
        return workflowService.getWorkflowById(id);
    }
    
    /**
     * 创建工作流
     */
    @PostMapping
    public Map<String, Object> createWorkflow(@RequestBody WorkflowDTO dto) {
        Long id = workflowService.saveWorkflow(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("message", "创建成功");
        return result;
    }
    
    /**
     * 更新工作流
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateWorkflow(@PathVariable Long id, @RequestBody WorkflowDTO dto) {
        dto.setId(id);
        workflowService.saveWorkflow(dto);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "更新成功");
        return result;
    }
    
    /**
     * 删除工作流
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        Map<String, Object> result = new HashMap<>();
        result.put("message", "删除成功");
        return result;
    }
    
    /**
     * 执行工作流
     */
    @PostMapping("/execute")
    public WorkflowExecuteResponse executeWorkflow(@RequestBody WorkflowExecuteRequest request) {
        return workflowService.executeWorkflow(request);
    }
}

