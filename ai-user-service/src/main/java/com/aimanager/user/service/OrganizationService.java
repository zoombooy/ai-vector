package com.aimanager.user.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.user.dto.OrganizationDTO;
import com.aimanager.user.entity.Organization;
import com.aimanager.user.entity.User;
import com.aimanager.user.mapper.OrganizationMapper;
import com.aimanager.user.mapper.UserMapper;
import com.aimanager.user.vo.OrganizationTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 组织服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationService {
    
    private final OrganizationMapper organizationMapper;
    private final UserMapper userMapper;
    
    /**
     * 获取组织树
     */
    public List<OrganizationTreeVO> getOrganizationTree() {
        // 查询所有组织
        List<Organization> allOrgs = organizationMapper.selectList(null);
        
        // 查询所有用户（用于获取负责人姓名）
        List<User> allUsers = userMapper.selectList(null);
        Map<Long, String> userMap = allUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getRealName, (k1, k2) -> k1));
        
        // 转换为VO
        List<OrganizationTreeVO> allVOs = allOrgs.stream().map(org -> {
            OrganizationTreeVO vo = new OrganizationTreeVO();
            BeanUtils.copyProperties(org, vo);
            if (org.getLeaderId() != null) {
                vo.setLeaderName(userMap.get(org.getLeaderId()));
            }
            return vo;
        }).collect(Collectors.toList());
        
        // 构建树形结构
        return buildTree(allVOs, 0L);
    }
    
    /**
     * 构建树形结构
     */
    private List<OrganizationTreeVO> buildTree(List<OrganizationTreeVO> allVOs, Long parentId) {
        List<OrganizationTreeVO> tree = new ArrayList<>();
        
        for (OrganizationTreeVO vo : allVOs) {
            if (parentId.equals(vo.getParentId())) {
                List<OrganizationTreeVO> children = buildTree(allVOs, vo.getId());
                if (!children.isEmpty()) {
                    vo.setChildren(children);
                }
                tree.add(vo);
            }
        }
        
        // 按排序字段排序
        tree.sort((o1, o2) -> {
            Integer sort1 = o1.getSort() != null ? o1.getSort() : 0;
            Integer sort2 = o2.getSort() != null ? o2.getSort() : 0;
            return sort1.compareTo(sort2);
        });
        
        return tree;
    }
    
    /**
     * 根据ID获取组织
     */
    public Organization getOrganizationById(Long id) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织不存在");
        }
        return org;
    }
    
    /**
     * 创建组织
     */
    public void createOrganization(OrganizationDTO orgDTO) {
        // 检查组织编码是否存在
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getOrgCode, orgDTO.getOrgCode());
        if (organizationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织编码已存在");
        }
        
        Organization org = new Organization();
        BeanUtils.copyProperties(orgDTO, org);
        
        // 设置默认值
        if (org.getParentId() == null) {
            org.setParentId(0L);
        }
        if (org.getStatus() == null) {
            org.setStatus(1);
        }
        
        // 计算组织层级和路径
        if (org.getParentId() == 0L) {
            org.setOrgLevel(1);
            org.setOrgPath("0");
        } else {
            Organization parent = organizationMapper.selectById(org.getParentId());
            if (parent == null) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "父组织不存在");
            }
            org.setOrgLevel(parent.getOrgLevel() + 1);
            org.setOrgPath(parent.getOrgPath() + "," + parent.getId());
        }
        
        organizationMapper.insert(org);
        
        log.info("创建组织成功: {}", org.getOrgName());
    }
    
    /**
     * 更新组织
     */
    public void updateOrganization(OrganizationDTO orgDTO) {
        Organization existOrg = organizationMapper.selectById(orgDTO.getId());
        if (existOrg == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织不存在");
        }
        
        // 检查组织编码是否被其他组织使用
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getOrgCode, orgDTO.getOrgCode())
                .ne(Organization::getId, orgDTO.getId());
        if (organizationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织编码已存在");
        }
        
        Organization org = new Organization();
        BeanUtils.copyProperties(orgDTO, org);
        
        organizationMapper.updateById(org);
        
        log.info("更新组织成功: {}", org.getOrgName());
    }
    
    /**
     * 删除组织
     */
    public void deleteOrganization(Long id) {
        Organization org = organizationMapper.selectById(id);
        if (org == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "组织不存在");
        }
        
        // 检查是否有子组织
        LambdaQueryWrapper<Organization> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Organization::getParentId, id);
        if (organizationMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该组织下有子组织，不能删除");
        }
        
        // 检查是否有用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getOrganizationId, id);
        if (userMapper.selectCount(userWrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "该组织下有用户，不能删除");
        }
        
        organizationMapper.deleteById(id);
        
        log.info("删除组织成功: {}", org.getOrgName());
    }
}

