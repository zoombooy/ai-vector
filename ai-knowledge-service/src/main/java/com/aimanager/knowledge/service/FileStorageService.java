package com.aimanager.knowledge.service;

import com.aimanager.common.exception.BusinessException;
import com.aimanager.common.result.ResultCode;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

/**
 * 文件存储服务（MinIO）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name:ai-knowledge}")
    private String bucketName;
    
    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 确保bucket存在
            ensureBucketExists();
            
            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("文件上传成功：{}", filename);
            return filename;
            
        } catch (Exception e) {
            log.error("文件上传失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR);
        }
    }
    
    /**
     * 下载文件
     */
    public InputStream downloadFile(String filename) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
        } catch (Exception e) {
            log.error("文件下载失败：{}", e.getMessage(), e);
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "文件下载失败");
        }
    }
    
    /**
     * 删除文件
     */
    public void deleteFile(String filename) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .build()
            );
            log.info("文件删除成功：{}", filename);
        } catch (Exception e) {
            log.error("文件删除失败：{}", e.getMessage(), e);
        }
    }
    
    /**
     * 确保bucket存在
     */
    private void ensureBucketExists() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build()
            );
            
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build()
                );
                log.info("创建Bucket：{}", bucketName);
            }
        } catch (Exception e) {
            log.error("检查Bucket失败：{}", e.getMessage(), e);
        }
    }
}

