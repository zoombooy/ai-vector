package com.aimanager.knowledge.controller;

import com.aimanager.common.result.Result;
import com.aimanager.knowledge.entity.Document;
import com.aimanager.knowledge.service.DocumentService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文档控制器
 */
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    
    /**
     * 上传文档
     */
    @PostMapping("/upload")
    public Result<Document> uploadDocument(
            @RequestParam(value = "categoryId") Long categoryId,
            @RequestParam(value = "title") String title,
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "tags", required = false) String tags) {

        Document document = documentService.uploadDocument(categoryId, title, file, tags);
        return Result.success(document);
    }
    
    /**
     * 分页查询文档
     */
    @GetMapping("/page")
    public Result<Page<Document>> pageDocuments(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword) {

        Page<Document> page = documentService.pageDocuments(pageNum, pageSize, categoryId, keyword);
        return Result.success(page);
    }
    
    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public Result<Document> getDocument(@PathVariable(value = "id") Long id) {
        Document document = documentService.getDocumentById(id);
        return Result.success(document);
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteDocument(@PathVariable(value = "id") Long id) {
        documentService.deleteDocument(id);
        return Result.success();
    }

    /**
     * 获取文档的指定分块内容
     */
    @GetMapping("/{id}/chunk/{chunkIndex}")
    public Result<Map<String, Object>> getDocumentChunk(
            @PathVariable(value = "id") Long id,
            @PathVariable(value = "chunkIndex") Integer chunkIndex) {
        Map<String, Object> chunk = documentService.getDocumentChunk(id, chunkIndex);
        return Result.success(chunk);
    }

    /**
     * 批量获取多个分块内容
     */
    @PostMapping("/chunks")
    public Result<List<Map<String, Object>>> getDocumentChunks(@RequestBody List<Map<String, Object>> chunkInfos) {
        List<Map<String, Object>> chunks = documentService.getDocumentChunks(chunkInfos);
        return Result.success(chunks);
    }

    /**
     * 重新向量化所有文档
     */
    @PostMapping("/revectorize-all")
    public Result<Map<String, Object>> revectorizeAllDocuments() {
        Map<String, Object> result = documentService.revectorizeAllDocuments();
        return Result.success(result);
    }

    /**
     * 重新向量化单个文档
     */
    @PostMapping("/{id}/revectorize")
    public Result<Integer> revectorizeDocument(@PathVariable(value = "id") Long id) {
        int chunkCount = documentService.revectorizeDocument(id);
        return Result.success(chunkCount);
    }
}

