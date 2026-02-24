package com.aimanager.knowledge.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import com.aimanager.knowledge.entity.Document;
import com.aimanager.knowledge.mapper.DocumentMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文档服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final FileStorageService fileStorageService;
    private final DocumentParserService documentParserService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${vector.service.url:http://localhost:8085}")
    private String vectorServiceUrl;
    
    /**
     * 上传文档
     */
    @Transactional(rollbackFor = Exception.class)
    public Document uploadDocument(Long categoryId, String title, MultipartFile file, String tags) {
        try {
            // 验证文件
            validateFile(file);
            
            // 上传文件到MinIO
            String filePath = fileStorageService.uploadFile(file);
            
            // 解析文档内容
            String content = documentParserService.parseDocument(file);
            
            // 创建文档记录
            Document document = new Document();
            document.setCategoryId(categoryId);
            document.setDocTitle(title);
            document.setDocType(getFileExtension(file.getOriginalFilename()));
            document.setFilePath(filePath);
            document.setFileSize(file.getSize());
            document.setContent(content);
            document.setTags(tags);
            document.setStatus(1); // 已发布
            document.setViewCount(0);
            document.setDownloadCount(0);
            
            documentMapper.insert(document);

            log.info("文档上传成功：{}", title);

            // 异步调用向量化服务
            vectorizeDocumentAsync(document.getId(), content);

            return document;
            
        } catch (IOException e) {
            log.error("文档上传失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }
    
    /**
     * 分页查询文档
     */
    public Page<Document> pageDocuments(int pageNum, int pageSize, Long categoryId, String keyword) {
        Page<Document> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        
        if (categoryId != null) {
            wrapper.eq(Document::getCategoryId, categoryId);
        }
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Document::getDocTitle, keyword)
                    .or().like(Document::getContent, keyword));
        }
        
        wrapper.orderByDesc(Document::getCreateTime);
        
        return documentMapper.selectPage(page, wrapper);
    }
    
    /**
     * 获取文档详情
     */
    public Document getDocumentById(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DOCUMENT_NOT_FOUND);
        }

        // 增加浏览次数
        document.setViewCount(document.getViewCount() + 1);
        documentMapper.updateById(document);

        return document;
    }

    /**
     * 获取文档的指定分块内容
     *
     * @param docId 文档ID
     * @param chunkIndex 分块索引
     * @return 分块内容和相关信息
     */
    public Map<String, Object> getDocumentChunk(Long docId, Integer chunkIndex) {
        Document document = documentMapper.selectById(docId);
        if (document == null) {
            throw new BusinessException(ResultCode.DOCUMENT_NOT_FOUND);
        }

        String content = document.getContent();
        if (content == null || content.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("docId", docId);
            result.put("docTitle", document.getDocTitle());
            result.put("chunkIndex", chunkIndex);
            result.put("chunkContent", "");
            result.put("totalChunks", 0);
            return result;
        }

        // 使用与向量化时相同的分块逻辑
        int chunkSize = 400;  // 与TextChunkService保持一致
        List<String> chunks = smartChunk(content, chunkSize);

        String chunkContent = "";
        if (chunkIndex >= 0 && chunkIndex < chunks.size()) {
            chunkContent = chunks.get(chunkIndex);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("docId", docId);
        result.put("docTitle", document.getDocTitle());
        result.put("chunkIndex", chunkIndex);
        result.put("chunkContent", chunkContent);
        result.put("totalChunks", chunks.size());

        return result;
    }

    /**
     * 批量获取多个分块内容
     */
    public List<Map<String, Object>> getDocumentChunks(List<Map<String, Object>> chunkInfos) {
        return chunkInfos.stream().map(info -> {
            Long docId = ((Number) info.get("docId")).longValue();
            Integer chunkIndex = ((Number) info.get("chunkIndex")).intValue();
            Map<String, Object> chunkData = getDocumentChunk(docId, chunkIndex);
            // 保留原始的score信息
            if (info.containsKey("score")) {
                chunkData.put("score", info.get("score"));
            }
            return chunkData;
        }).toList();
    }

    /**
     * 智能分块（与向量服务的分块逻辑一致）
     */
    private List<String> smartChunk(String text, int chunkSize) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        if (text.length() <= chunkSize) {
            return List.of(text.trim());
        }

        List<String> chunks = new java.util.ArrayList<>();
        StringBuilder currentChunk = new StringBuilder(chunkSize);
        StringBuilder currentLine = new StringBuilder(200);

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                String line = currentLine.toString().trim();
                if (!line.isEmpty()) {
                    if (currentChunk.length() + line.length() + 1 > chunkSize) {
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk.setLength(0);
                        }
                    }
                    if (currentChunk.length() > 0) {
                        currentChunk.append('\n');
                    }
                    currentChunk.append(line);
                }
                currentLine.setLength(0);
            } else {
                currentLine.append(c);
            }
        }

        // 处理最后一行
        if (currentLine.length() > 0) {
            String line = currentLine.toString().trim();
            if (!line.isEmpty()) {
                if (currentChunk.length() + line.length() + 1 > chunkSize) {
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk.setLength(0);
                    }
                }
                if (currentChunk.length() > 0) {
                    currentChunk.append('\n');
                }
                currentChunk.append(line);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        // 如果没有生成任何块，使用简单分块
        if (chunks.isEmpty()) {
            for (int i = 0; i < text.length(); i += chunkSize) {
                int end = Math.min(i + chunkSize, text.length());
                String chunk = text.substring(i, end).trim();
                if (!chunk.isEmpty()) {
                    chunks.add(chunk);
                }
            }
        }

        return chunks;
    }
    
    /**
     * 删除文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDocument(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DOCUMENT_NOT_FOUND);
        }
        
        // 删除文件
        fileStorageService.deleteFile(document.getFilePath());
        
        // 删除数据库记录
        documentMapper.deleteById(id);
        
        log.info("文档删除成功：{}", document.getDocTitle());
    }
    
    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "文件不能为空");
        }
        
        // 检查文件大小（50MB）
        if (file.getSize() > 50 * 1024 * 1024) {
            throw new BusinessException(ResultCode.FILE_SIZE_EXCEED);
        }
        
        // 检查文件类型
        String filename = file.getOriginalFilename();
        if (filename == null || !isSupportedFileType(filename)) {
            throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        }
    }
    
    /**
     * 判断是否支持的文件类型
     */
    private boolean isSupportedFileType(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.matches("(pdf|doc|docx|txt|md)");
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }

    /**
     * 异步调用向量化服务
     */
    private void vectorizeDocumentAsync(Long documentId, String content) {
        new Thread(() -> {
            try {
                log.info("开始向量化文档: documentId={}", documentId);

                // 构建请求参数
                MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
                params.add("documentId", documentId);
                params.add("content", content);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

                // 调用向量服务
                String url = vectorServiceUrl + "/vector/vectorize";
                ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK) {
                    log.info("文档向量化成功: documentId={}", documentId);
                } else {
                    log.error("文档向量化失败: documentId={}, status={}", documentId, response.getStatusCode());
                }

            } catch (Exception e) {
                log.error("文档向量化异常: documentId={}, error={}", documentId, e.getMessage(), e);
            }
        }).start();
    }

    /**
     * 同步调用向量化服务（返回分块数量）
     */
    private int vectorizeDocumentSync(Long documentId, String content) {
        try {
            log.info("开始同步向量化文档: documentId={}", documentId);

            // 先删除旧的向量
            try {
                String deleteUrl = vectorServiceUrl + "/vector/delete/" + documentId;
                restTemplate.delete(deleteUrl);
                log.info("已删除文档旧向量: documentId={}", documentId);
            } catch (Exception e) {
                log.warn("删除旧向量失败（可能不存在）: documentId={}", documentId);
            }

            // 构建请求参数
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("documentId", documentId);
            params.add("content", content);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(params, headers);

            // 调用向量服务
            String url = vectorServiceUrl + "/vector/vectorize";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                // 解析返回的分块数量
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode jsonNode = mapper.readTree(response.getBody());
                int chunkCount = jsonNode.path("data").asInt(0);
                log.info("文档向量化成功: documentId={}, 分块数={}", documentId, chunkCount);
                return chunkCount;
            } else {
                log.error("文档向量化失败: documentId={}, status={}", documentId, response.getStatusCode());
                return 0;
            }

        } catch (Exception e) {
            log.error("文档向量化异常: documentId={}, error={}", documentId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 重新向量化单个文档
     */
    public int revectorizeDocument(Long id) {
        Document document = documentMapper.selectById(id);
        if (document == null) {
            throw new BusinessException(ResultCode.DOCUMENT_NOT_FOUND);
        }

        String content = document.getContent();
        if (content == null || content.isEmpty()) {
            log.warn("文档内容为空，跳过向量化: documentId={}", id);
            return 0;
        }

        return vectorizeDocumentSync(id, content);
    }

    /**
     * 重新向量化所有文档
     */
    public Map<String, Object> revectorizeAllDocuments() {
        log.info("===== 开始重新向量化所有文档 =====");

        // 1. 先清空向量库
        try {
            String resetUrl = vectorServiceUrl + "/vector/reset-collection";
            restTemplate.postForEntity(resetUrl, null, String.class);
            log.info("向量库已清空");
        } catch (Exception e) {
            log.error("清空向量库失败: {}", e.getMessage());
        }

        // 2. 查询所有文档
        LambdaQueryWrapper<Document> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNotNull(Document::getContent);
        wrapper.ne(Document::getContent, "");
        List<Document> documents = documentMapper.selectList(wrapper);

        log.info("找到 {} 个有内容的文档需要向量化", documents.size());

        int successCount = 0;
        int failCount = 0;
        int totalChunks = 0;

        for (Document doc : documents) {
            try {
                int chunks = vectorizeDocumentSync(doc.getId(), doc.getContent());
                if (chunks > 0) {
                    successCount++;
                    totalChunks += chunks;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("向量化文档失败: documentId={}, title={}, error={}",
                    doc.getId(), doc.getDocTitle(), e.getMessage());
                failCount++;
            }
        }

        log.info("===== 重新向量化完成: 成功{}个, 失败{}个, 总分块数={} =====",
            successCount, failCount, totalChunks);

        Map<String, Object> result = new HashMap<>();
        result.put("totalDocuments", documents.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("totalChunks", totalChunks);

        return result;
    }
}

