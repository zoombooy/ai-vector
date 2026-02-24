package com.aimanager.knowledge.entity;

import com.aimanager.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_document")
public class Document extends BaseEntity {
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 文档标题
     */
    private String docTitle;
    
    /**
     * 文档类型
     */
    private String docType;
    
    /**
     * 文件路径
     */
    private String filePath;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 文档内容
     */
    private String content;
    
    /**
     * 摘要
     */
    private String summary;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 状态（0-草稿，1-已发布，2-已归档）
     */
    private Integer status;
}

