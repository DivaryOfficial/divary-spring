package com.divary.domain.image.service;

import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
// 이미지 저장 서비스
public class ImageStorageService {

    private final S3Client s3Client;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static}")
    private String region;

    // S3에 파일 업로드
    public void uploadToS3(String s3Key, MultipartFile file) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, 
                              RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            log.info("S3 업로드 완료: {}", s3Key);
        } catch (IOException e) {
            log.error("S3 업로드 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // S3에서 파일 삭제
    public void deleteFromS3(String s3Key) {
        try {
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(s3Key));
            log.info("S3 삭제 완료: {}", s3Key);
        } catch (Exception e) {
            log.error("S3 삭제 실패: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // S3 키로부터 Public URL 생성
    public String generatePublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    // 고유한 파일명 생성
    public String generateUniqueFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    // S3 키 생성 (경로 + 파일명)
    public String generateS3Key(String uploadPath, String fileName) {
        String normalizedPath = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        return normalizedPath + fileName;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
} 