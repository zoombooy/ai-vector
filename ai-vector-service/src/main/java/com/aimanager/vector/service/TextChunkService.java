package com.aimanager.vector.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块服务 - 极简内存优化版本
 * 使用字符级别的流式处理，避免 substring() 创建大量字符串对象
 */
@Slf4j
@Service
public class TextChunkService {

    @Value("${embedding.chunk-size:500}")
    private Integer chunkSize;

    @Value("${embedding.chunk-overlap:50}")
    private Integer chunkOverlap;

    /**
     * 将长文本分块 - 使用 StringBuilder 逐字符处理，避免 substring
     *
     * @param text 原始文本
     * @return 文本块列表
     */
    public List<String> chunkText(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();
        int textLength = text.length();
        int actualChunkSize = Math.min(chunkSize, 400);  // 限制分块大小

        StringBuilder currentChunk = new StringBuilder(actualChunkSize);
        int charCount = 0;

        // 逐字符处理，避免 substring
        for (int i = 0; i < textLength; i++) {
            char c = text.charAt(i);
            currentChunk.append(c);
            charCount++;

            // 达到分块大小
            if (charCount >= actualChunkSize) {
                String chunk = currentChunk.toString().trim();
                if (!chunk.isEmpty()) {
                    chunks.add(chunk);
                }

                // 重置
                currentChunk.setLength(0);
                charCount = 0;
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            String chunk = currentChunk.toString().trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
        }

        log.debug("文本分块完成: 原始长度={}, 分块数={}", textLength, chunks.size());
        return chunks;
    }

    /**
     * 智能分块 - 按行处理，避免一次性加载整个文档
     *
     * @param text 原始文本
     * @return 文本块列表
     */
    public List<String> smartChunk(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        int actualChunkSize = Math.min(chunkSize, 400);
        log.info("分块参数: chunkSize配置={}, actualChunkSize={}, 文本长度={}", chunkSize, actualChunkSize, text.length());

        // 如果文本很小，直接返回（使用实际分块大小判断）
        if (text.length() <= actualChunkSize) {
            log.info("文本长度小于分块大小，作为单一分块返回");
            return List.of(text.trim());
        }

        log.info("开始智能分块: 原始长度={}", text.length());

        // 检查文本中是否有换行符
        int newlineCount = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') newlineCount++;
        }
        log.info("文本中换行符数量: {}", newlineCount);

        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder(actualChunkSize);
        StringBuilder currentLine = new StringBuilder(200);

        int textLength = text.length();

        // 逐字符处理，按行分块
        for (int i = 0; i < textLength; i++) {
            char c = text.charAt(i);

            if (c == '\n') {
                // 遇到换行符，处理当前行
                String line = currentLine.toString().trim();

                if (!line.isEmpty()) {
                    // 如果这一行本身就超过分块大小，需要强制拆分
                    if (line.length() > actualChunkSize) {
                        // 先保存当前块
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk.setLength(0);
                        }
                        // 强制按字符拆分超长行
                        for (int j = 0; j < line.length(); j += actualChunkSize) {
                            int end = Math.min(j + actualChunkSize, line.length());
                            chunks.add(line.substring(j, end).trim());
                        }
                    } else {
                        // 检查是否会超过分块大小
                        if (currentChunk.length() + line.length() + 1 > actualChunkSize) {
                            // 保存当前块
                            if (currentChunk.length() > 0) {
                                chunks.add(currentChunk.toString().trim());
                                log.debug("生成分块 #{}: 长度={}", chunks.size(), chunks.get(chunks.size()-1).length());
                                currentChunk.setLength(0);
                            }
                        }

                        // 添加行到当前块
                        if (currentChunk.length() > 0) {
                            currentChunk.append('\n');
                        }
                        currentChunk.append(line);
                    }
                }

                // 重置当前行
                currentLine.setLength(0);
            } else {
                // 添加字符到当前行
                currentLine.append(c);
            }
        }

        // 处理最后一行（可能是超长行，需要拆分）
        if (currentLine.length() > 0) {
            String line = currentLine.toString().trim();
            if (!line.isEmpty()) {
                // 如果这一行本身就超过分块大小，需要强制拆分
                if (line.length() > actualChunkSize) {
                    log.info("发现超长行，长度={}，需要强制拆分", line.length());
                    // 先保存当前块
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString().trim());
                        currentChunk.setLength(0);
                    }
                    // 强制按字符拆分超长行
                    for (int j = 0; j < line.length(); j += actualChunkSize) {
                        int end = Math.min(j + actualChunkSize, line.length());
                        chunks.add(line.substring(j, end).trim());
                        log.debug("强制拆分生成分块 #{}: 长度={}", chunks.size(), chunks.get(chunks.size()-1).length());
                    }
                } else {
                    if (currentChunk.length() + line.length() + 1 > actualChunkSize) {
                        if (currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString().trim());
                            log.debug("生成分块 #{}: 长度={}", chunks.size(), chunks.get(chunks.size()-1).length());
                            currentChunk.setLength(0);
                        }
                    }
                    if (currentChunk.length() > 0) {
                        currentChunk.append('\n');
                    }
                    currentChunk.append(line);
                }
            }
        }

        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
            log.debug("生成最后分块 #{}: 长度={}", chunks.size(), chunks.get(chunks.size()-1).length());
        }

        // 如果没有生成任何块，使用简单分块
        if (chunks.isEmpty()) {
            log.warn("智能分块未生成任何块，回退到简单分块");
            return chunkText(text);
        }

        log.info("智能分块完成: 原始长度={}, 分块数={}", textLength, chunks.size());
        for (int i = 0; i < chunks.size(); i++) {
            log.info("  分块[{}]: 长度={}, 前50字符={}", i, chunks.get(i).length(),
                chunks.get(i).substring(0, Math.min(50, chunks.get(i).length())));
        }
        return chunks;
    }
}

