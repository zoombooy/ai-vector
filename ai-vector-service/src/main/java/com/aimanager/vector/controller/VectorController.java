package com.aimanager.vector.controller;

import com.aimanager.common.result.Result;
import com.aimanager.vector.service.VectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 向量处理控制器
 */
@RestController
@RequestMapping("/vector")
@RequiredArgsConstructor
public class VectorController {
    
    private final VectorService vectorService;
    
    /**
     * 文档向量化
     */
    @PostMapping(value = "/vectorize", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result<Integer> vectorizeDocument(
            @RequestParam(value = "documentId") Long documentId,
            @RequestParam(value = "content") String content) {

        // 限制文档大小（5MB = 5,242,880 字节，约 2,621,440 个中文字符）
        if (content.length() > 2_000_000) {
            return Result.fail("文档过大（超过2百万字符），请分割后上传。当前长度: " + content.length());
        }

        int chunkCount = vectorService.vectorizeDocument(documentId, content);
        return Result.success(chunkCount);
    }

    /**
     * 向量检索（返回文档ID列表）
     */
    @GetMapping("/search")
    public Result<List<Long>> searchSimilarDocuments(
            @RequestParam(value = "queryText") String queryText,
            @RequestParam(value = "topK", defaultValue = "5") Integer topK) {

        List<Long> documentIds = vectorService.searchSimilarDocuments(queryText, topK);
        return Result.success(documentIds);
    }

    /**
     * 向量检索（返回分块详情，包括docId、chunkIndex、score）
     */
    @GetMapping("/search/chunks")
    public Result<List<Map<String, Object>>> searchSimilarChunks(
            @RequestParam(value = "queryText") String queryText,
            @RequestParam(value = "topK", defaultValue = "10") Integer topK) {

        List<Map<String, Object>> chunks = vectorService.searchSimilarChunks(queryText, topK);
        return Result.success(chunks);
    }

    /**
     * 删除文档向量
     */
    @DeleteMapping("/delete/{documentId}")
    public Result<Void> deleteDocumentVectors(@PathVariable(value = "documentId") Long documentId) {
        vectorService.deleteDocumentVectors(documentId);
        return Result.success();
    }

    /**
     * 清空向量 Collection
     */
    @PostMapping("/reset-collection")
    public Result<Void> resetCollection() {
        vectorService.resetCollection();
        return Result.success();
    }

    /**
     * 获取 Collection 统计信息
     */
    @GetMapping("/collection-stats")
    public Result<Map<String, Object>> getCollectionStats() {
        Map<String, Object> stats = vectorService.getCollectionStats();
        return Result.success(stats);
    }
}

