package com.aimanager.user.controller;

import com.aimanager.common.result.Result;
import com.aimanager.user.dto.OrganizationDTO;
import com.aimanager.user.entity.Organization;
import com.aimanager.user.service.OrganizationService;
import com.aimanager.user.vo.OrganizationTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织管理控制器
 */
@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {
    
    private final OrganizationService organizationService;
    
    /**
     * 获取组织树
     */
    @GetMapping("/tree")
    public Result<List<OrganizationTreeVO>> getOrganizationTree() {
        List<OrganizationTreeVO> tree = organizationService.getOrganizationTree();
        return Result.success(tree);
    }
    
    /**
     * 根据ID获取组织
     */
    @GetMapping("/{id}")
    public Result<Organization> getOrganizationById(@PathVariable Long id) {
        Organization org = organizationService.getOrganizationById(id);
        return Result.success(org);
    }
    
    /**
     * 创建组织
     */
    @PostMapping
    public Result<Void> createOrganization(@Validated @RequestBody OrganizationDTO orgDTO) {
        organizationService.createOrganization(orgDTO);
        return Result.success();
    }
    
    /**
     * 更新组织
     */
    @PutMapping
    public Result<Void> updateOrganization(@Validated @RequestBody OrganizationDTO orgDTO) {
        organizationService.updateOrganization(orgDTO);
        return Result.success();
    }
    
    /**
     * 删除组织
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return Result.success();
    }
}

