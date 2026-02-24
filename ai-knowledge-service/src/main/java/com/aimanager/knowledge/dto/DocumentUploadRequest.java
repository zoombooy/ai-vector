package com.aimanager.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传请求DTO
 */
@Data
public class DocumentUploadRequest {
    
    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    private String docTitle;
    
    /**
     * 文件
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 描述
     */
    private String description;
}

