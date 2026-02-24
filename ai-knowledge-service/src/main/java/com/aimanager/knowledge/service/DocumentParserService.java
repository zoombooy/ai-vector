package com.aimanager.knowledge.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文档解析服务
 */
@Slf4j
@Service
public class DocumentParserService {
    
    /**
     * 解析文档内容
     */
    public String parseDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new BusinessException(ResultCode.DOCUMENT_PARSE_ERROR);
        }
        
        String extension = getFileExtension(filename).toLowerCase();
        
        return switch (extension) {
            case "pdf" -> parsePdf(file);
            case "doc", "docx" -> parseWord(file);
            case "txt", "md" -> parseText(file);
            default -> throw new BusinessException(ResultCode.FILE_TYPE_ERROR);
        };
    }
    
    /**
     * 解析PDF文档
     */
    private String parsePdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("PDF解析成功，页数：{}", document.getNumberOfPages());
            return text;
        } catch (Exception e) {
            log.error("PDF解析失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.DOCUMENT_PARSE_ERROR);
        }
    }
    
    /**
     * 解析Word文档（支持段落和表格）
     */
    private String parseWord(MultipartFile file) throws IOException {
        try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
            StringBuilder content = new StringBuilder();

            int paragraphCount = 0;
            int tableCount = 0;

            // 按文档元素顺序遍历（保持段落和表格的原始顺序）
            for (IBodyElement element : document.getBodyElements()) {
                if (element instanceof XWPFParagraph) {
                    XWPFParagraph paragraph = (XWPFParagraph) element;
                    String text = paragraph.getText();
                    if (text != null && !text.trim().isEmpty()) {
                        content.append(text.trim()).append("\n");
                        paragraphCount++;
                    }
                } else if (element instanceof XWPFTable) {
                    XWPFTable table = (XWPFTable) element;
                    tableCount++;
                    // 遍历表格的每一行
                    for (XWPFTableRow row : table.getRows()) {
                        StringBuilder rowContent = new StringBuilder();
                        // 遍历行中的每个单元格
                        for (XWPFTableCell cell : row.getTableCells()) {
                            String cellText = cell.getText();
                            if (cellText != null && !cellText.trim().isEmpty()) {
                                if (rowContent.length() > 0) {
                                    rowContent.append("\t");  // 用制表符分隔单元格
                                }
                                rowContent.append(cellText.trim());
                            }
                        }
                        if (rowContent.length() > 0) {
                            content.append(rowContent).append("\n");
                        }
                    }
                    content.append("\n");  // 表格后空一行
                }
            }

            log.info("Word解析成功，段落数：{}，表格数：{}，总字符数：{}",
                paragraphCount, tableCount, content.length());
            return content.toString();
        } catch (Exception e) {
            log.error("Word解析失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.DOCUMENT_PARSE_ERROR);
        }
    }
    
    /**
     * 解析文本文档
     */
    private String parseText(MultipartFile file) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            log.info("文本解析成功");
            return content.toString();
        } catch (Exception e) {
            log.error("文本解析失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.DOCUMENT_PARSE_ERROR);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot + 1) : "";
    }
}

