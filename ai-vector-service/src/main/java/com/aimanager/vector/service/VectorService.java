package com.aimanager.vector.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.GetCollectionStatisticsResponse;
import io.milvus.grpc.SearchResults;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 向量处理服务 - 基于 Milvus
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VectorService {

    private final MilvusServiceClient milvusClient;
    private final EmbeddingService embeddingService;
    private final TextChunkService textChunkService;

    @Value("${milvus.collection-name:knowledge_vectors}")
    private String collectionName;

    @Value("${embedding.dimension:1536}")
    private Integer dimension;




    /**
     * 初始化 Milvus 集合
     */
    @PostConstruct
    public void initCollection() {
        try {
            // 检查集合是否存在
            R<Boolean> hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (hasCollection.getData()) {
                log.info("Milvus 集合已存在: {}", collectionName);
                // 集合已存在，确保索引已创建
                ensureIndexExists();
                // 确保已加载到内存
                loadCollection();
                log.info("Milvus 集合初始化成功: {}", collectionName);
                return;
            }

            // 创建集合
            createCollection();

            // 创建索引
            createIndex();

            // 加载集合
            loadCollection();

            log.info("Milvus 集合初始化成功: {}", collectionName);

        } catch (Exception e) {
            log.error("Milvus 集合初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 创建集合
     */
    private void createCollection() {
        FieldType docIdField = FieldType.newBuilder()
                .withName("doc_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(false)
                .build();

        FieldType chunkIdField = FieldType.newBuilder()
                .withName("chunk_id")
                .withDataType(DataType.Int64)
                .withPrimaryKey(true)
                .withAutoID(true)
                .build();

        FieldType chunkIndexField = FieldType.newBuilder()
                .withName("chunk_index")
                .withDataType(DataType.Int64)
                .build();

        FieldType vectorField = FieldType.newBuilder()
                .withName("embedding")
                .withDataType(DataType.FloatVector)
                .withDimension(dimension)
                .build();

        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)
                .withDescription("知识库文档向量集合")
                .addFieldType(docIdField)
                .addFieldType(chunkIdField)
                .addFieldType(chunkIndexField)
                .addFieldType(vectorField)
                .build();

        R<RpcStatus> response = milvusClient.createCollection(createParam);

        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("创建集合失败: " + response.getMessage());
        }

        log.info("Milvus 集合创建成功");
    }

    /**
     * 确保索引存在
     */
    private void ensureIndexExists() {
        try {
            // 尝试创建索引
            createIndex();
        } catch (Exception e) {
            // 如果索引已存在，忽略错误
            if (e.getMessage() != null && e.getMessage().contains("index already exist")) {
                log.info("Milvus 索引已存在");
            } else {
                log.error("创建索引失败: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

    /**
     * 创建索引
     */
    private void createIndex() {
        // 使用 FLAT 索引（适合小数据集，精确搜索）
        // 如果数据量大（>100万），可以改用 IVF_FLAT
        CreateIndexParam indexParam = CreateIndexParam.newBuilder()
                .withCollectionName(collectionName)
                .withFieldName("embedding")
                .withIndexType(io.milvus.param.IndexType.FLAT)  // 改为 FLAT
                .withMetricType(io.milvus.param.MetricType.L2)
                .build();

        R<RpcStatus> response = milvusClient.createIndex(indexParam);

        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("创建索引失败: " + response.getMessage());
        }

        log.info("Milvus 索引创建成功（类型: FLAT）");
    }

    /**
     * 加载集合到内存
     */
    private void loadCollection() {
        try {
            // 尝试加载集合
            R<RpcStatus> response = milvusClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (response.getStatus() == R.Status.Success.getCode()) {
                log.info("Milvus 集合加载成功");
                return;
            }

            // 检查是否是因为已经加载
            String errorMsg = response.getMessage();
            if (errorMsg != null && (errorMsg.contains("already loaded") || errorMsg.contains("loaded"))) {
                log.info("Milvus 集合已加载");
                return;
            }

            log.warn("加载集合返回非成功状态: {}", errorMsg);

        } catch (Exception e) {
            // 如果异常信息包含 "already loaded"，则忽略
            if (e.getMessage() != null && e.getMessage().contains("already loaded")) {
                log.info("Milvus 集合已加载");
                return;
            }
            log.error("加载集合失败: {}", e.getMessage(), e);
            throw new RuntimeException("加载集合失败: " + e.getMessage());
        }
    }

    /**
     * 文档向量化并存储
     *
     * @param documentId 文档ID
     * @param content 文档内容
     * @return 向量化的分块数量
     */
    public int vectorizeDocument(Long documentId, String content) {
        log.info("开始向量化文档: documentId={}, 内容长度={}", documentId, content.length());

        try {
            // 检查文档大小
            if (content.length() > 2_000_000) {  // 2百万字符
                throw new RuntimeException("文档过大（超过2百万字符），请分割后上传。当前长度: " + content.length());
            }

            // 1. 文本分块
            List<String> chunks = textChunkService.smartChunk(content);
            log.info("文档分块完成: documentId={}, 分块数={}", documentId, chunks.size());

            if (chunks.isEmpty()) {
                log.warn("文档内容为空，跳过向量化");
                return 0;
            }

            // 2. 逐个向量化并存储（避免批量处理占用内存）
            int totalChunks = chunks.size();
            int processedChunks = 0;
            int batchSize = 10;  // 进一步减小批处理大小
            List<List<Float>> batchVectors = new ArrayList<>(batchSize);

            for (int i = 0; i < totalChunks; i++) {
                try {
                    // 向量化单个分块
                    List<Float> vector = embeddingService.textToVector(chunks.get(i));
                    batchVectors.add(vector);

                    // 每 10 个分块存储一次
                    if (batchVectors.size() >= batchSize || i == totalChunks - 1) {
                        storeVectorsBatch(documentId, batchVectors, processedChunks);
                        processedChunks += batchVectors.size();

                        log.info("批次处理完成: 已处理 {}/{} 个分块", processedChunks, totalChunks);

                        // 清理内存
                        batchVectors.clear();

                        // 每处理 50 个分块建议 GC
                        if (processedChunks % 50 == 0) {
                            System.gc();
                        }
                    }

                } catch (Exception e) {
                    log.error("分块向量化失败: documentId={}, chunkIndex={}, error={}",
                        documentId, i, e.getMessage(), e);
                    // 如果是第一个分块失败，抛出异常以便用户知道问题
                    if (i == 0) {
                        throw new RuntimeException("向量化失败: " + e.getMessage());
                    }
                    // 继续处理下一个分块
                }
            }

            log.info("文档向量化完成: documentId={}, 总分块数={}", documentId, processedChunks);
            return processedChunks;

        } catch (OutOfMemoryError e) {
            log.error("内存溢出: documentId={}, 内容长度={}", documentId, content.length(), e);
            throw new RuntimeException("内存不足，无法处理该文档");
        } catch (Exception e) {
            log.error("文档向量化失败: documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("文档向量化失败: " + e.getMessage());
        }
    }

    /**
     * 存储向量到 Milvus
     */
    private void storeVectors(Long documentId, List<List<Float>> vectors) {
        storeVectorsBatch(documentId, vectors, 0);
    }

    /**
     * 批量存储向量到 Milvus
     *
     * @param documentId 文档ID
     * @param vectors 向量列表
     * @param startIndex 起始索引（用于分块索引）
     */
    private void storeVectorsBatch(Long documentId, List<List<Float>> vectors, int startIndex) {
        if (vectors.isEmpty()) {
            return;
        }

        List<Long> docIds = new ArrayList<>();
        List<Long> chunkIndexes = new ArrayList<>();

        for (int i = 0; i < vectors.size(); i++) {
            docIds.add(documentId);
            chunkIndexes.add((long) (startIndex + i));
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("doc_id", docIds));
        fields.add(new InsertParam.Field("chunk_index", chunkIndexes));
        fields.add(new InsertParam.Field("embedding", vectors));

        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withFields(fields)
                .build();

        R<io.milvus.grpc.MutationResult> response = milvusClient.insert(insertParam);

        if (response.getStatus() != R.Status.Success.getCode()) {
            throw new RuntimeException("向量存储失败: " + response.getMessage());
        }

        log.info("向量批量存储成功: documentId={}, 向量数={}, 起始索引={}",
            documentId, vectors.size(), startIndex);
    }

    /**
     * 向量检索 - 查找相似文档
     *
     * @param queryText 查询文本
     * @param topK 返回前K个结果
     * @return 文档ID列表
     */
    public List<Long> searchSimilarDocuments(String queryText, Integer topK) {
        log.info("开始向量检索: queryText={}, topK={}", queryText, topK);

        try {
            // 1. 将查询文本向量化
            List<Float> queryVector = embeddingService.textToVector(queryText);

            // 2. 在 Milvus 中搜索
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(io.milvus.param.MetricType.L2)
                    .withOutFields(List.of("doc_id", "chunk_index"))
                    .withTopK(topK)
                    .withVectors(List.of(queryVector))
                    .withVectorFieldName("embedding")
                    // FLAT 索引不需要 nprobe 参数
                    .build();

            R<SearchResults> response = milvusClient.search(searchParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("向量检索失败: " + response.getMessage());
            }

            // 3. 解析结果
            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            List<Long> documentIds = new ArrayList<>();

            for (int i = 0; i < wrapper.getRowRecords(0).size(); i++) {
                Long docId = (Long) wrapper.getRowRecords(0).get(i).get("doc_id");
                if (!documentIds.contains(docId)) {
                    documentIds.add(docId);
                }
            }

            log.info("向量检索完成: 找到{}个相关文档", documentIds.size());
            return documentIds;

        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量检索失败: " + e.getMessage());
        }
    }

    /**
     * 向量检索 - 查找相似文档分块（返回详细信息）
     *
     * @param queryText 查询文本
     * @param topK 返回前K个结果
     * @return 分块信息列表 [{docId, chunkIndex, score}]
     */
    public List<Map<String, Object>> searchSimilarChunks(String queryText, Integer topK) {
        log.info("开始向量检索(分块): queryText={}, topK={}", queryText, topK);

        try {
            // 1. 将查询文本向量化
            List<Float> queryVector = embeddingService.textToVector(queryText);

            // 2. 在 Milvus 中搜索
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withMetricType(io.milvus.param.MetricType.L2)
                    .withOutFields(List.of("doc_id", "chunk_index"))
                    .withTopK(topK)
                    .withVectors(List.of(queryVector))
                    .withVectorFieldName("embedding")
                    .build();

            R<SearchResults> response = milvusClient.search(searchParam);

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("向量检索失败: " + response.getMessage());
            }

            // 3. 解析结果，包含分块信息
            SearchResultsWrapper wrapper = new SearchResultsWrapper(response.getData().getResults());
            List<Map<String, Object>> results = new ArrayList<>();

            List<SearchResultsWrapper.IDScore> scores = wrapper.getIDScore(0);

            // 使用 getFieldData 获取字段值列表
            List<?> docIdList = (List<?>) wrapper.getFieldData("doc_id", 0);
            List<?> chunkIndexList = (List<?>) wrapper.getFieldData("chunk_index", 0);

            for (int i = 0; i < scores.size(); i++) {
                // 从字段列表中获取值
                Long docId = ((Number) docIdList.get(i)).longValue();
                Integer chunkIndex = ((Number) chunkIndexList.get(i)).intValue();
                float score = scores.get(i).getScore();

                Map<String, Object> result = new HashMap<>();
                result.put("docId", docId);
                result.put("chunkIndex", chunkIndex);
                result.put("score", score);
                results.add(result);

                log.debug("检索结果: docId={}, chunkIndex={}, score={}", docId, chunkIndex, score);
            }

            log.info("向量检索(分块)完成: 找到{}个相关分块", results.size());
            return results;

        } catch (Exception e) {
            log.error("向量检索(分块)失败: {}", e.getMessage(), e);
            throw new RuntimeException("向量检索失败: " + e.getMessage());
        }
    }

    /**
     * 删除文档的所有向量
     *
     * @param documentId 文档ID
     */
    public void deleteDocumentVectors(Long documentId) {
        log.info("删除文档向量: documentId={}", documentId);

        try {
            String expr = "doc_id == " + documentId;

            R<io.milvus.grpc.MutationResult> response = milvusClient.delete(
                    io.milvus.param.dml.DeleteParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withExpr(expr)
                            .build()
            );

            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new RuntimeException("删除向量失败: " + response.getMessage());
            }

            log.info("文档向量删除成功: documentId={}", documentId);

        } catch (Exception e) {
            log.error("删除文档向量失败: documentId={}, error={}", documentId, e.getMessage(), e);
            throw new RuntimeException("删除文档向量失败: " + e.getMessage());
        }
    }

    /**
     * 清空向量 Collection（删除并重新创建）
     */
    public void resetCollection() {
        log.info("开始清空向量 Collection: {}", collectionName);

        try {
            // 1. 检查 Collection 是否存在
            R<Boolean> hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (hasCollection.getData()) {
                // 2. 删除 Collection
                DropCollectionParam dropParam = DropCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build();

                R<RpcStatus> dropResponse = milvusClient.dropCollection(dropParam);

                if (dropResponse.getStatus() != R.Status.Success.getCode()) {
                    throw new RuntimeException("删除 Collection 失败: " + dropResponse.getMessage());
                }

                log.info("Collection 删除成功: {}", collectionName);
            } else {
                log.info("Collection 不存在，无需删除: {}", collectionName);
            }

            // 3. 重新创建 Collection
            createCollection();

            // 4. 创建索引
            createIndex();

            // 5. 加载 Collection
            loadCollection();

            log.info("向量 Collection 清空成功，已重新创建: {}", collectionName);

        } catch (Exception e) {
            log.error("清空向量 Collection 失败: {}", e.getMessage(), e);
            throw new RuntimeException("清空向量 Collection 失败: " + e.getMessage());
        }
    }

    /**
     * 获取 Collection 统计信息
     */
    public Map<String, Object> getCollectionStats() {
        log.info("获取 Collection 统计信息: {}", collectionName);

        Map<String, Object> stats = new HashMap<>();

        try {
            // 检查 Collection 是否存在
            R<Boolean> hasCollection = milvusClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (!hasCollection.getData()) {
                stats.put("exists", false);
                stats.put("collectionName", collectionName);
                return stats;
            }

            stats.put("exists", true);
            stats.put("collectionName", collectionName);

            // 获取实体数量
            R<GetCollectionStatisticsResponse> statsResponse = milvusClient.getCollectionStatistics(
                    GetCollectionStatisticsParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build()
            );

            if (statsResponse.getStatus() == R.Status.Success.getCode()) {
                long rowCount = statsResponse.getData().getStatsCount();
                stats.put("rowCount", rowCount);
                stats.put("vectorCount", rowCount);
            } else {
                stats.put("rowCount", "unknown");
                stats.put("error", statsResponse.getMessage());
            }

            log.info("Collection 统计信息: {}", stats);

        } catch (Exception e) {
            log.error("获取 Collection 统计信息失败: {}", e.getMessage(), e);
            stats.put("exists", false);
            stats.put("error", e.getMessage());
        }

        return stats;
    }
}

