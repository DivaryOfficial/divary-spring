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
import java.util.List;
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

    // S3에서 파일을 다른 경로로 이동 (복사 + 삭제)
    public void moveFile(String sourceKey, String destinationKey) {
        try {
            // 1. 파일 복사
            s3Client.copyObject(builder -> builder
                    .sourceBucket(bucketName)
                    .sourceKey(sourceKey)
                    .destinationBucket(bucketName)
                    .destinationKey(destinationKey)
            );
            log.info("S3 파일 복사 완료: {} -> {}", sourceKey, destinationKey);
            
            // 2. 복사 성공 확인 후 원본 파일 삭제
            if (verifyFileExists(destinationKey)) {
                deleteFromS3(sourceKey);
                log.info("S3 파일 이동 완료: {} -> {}", sourceKey, destinationKey);
            } else {
                log.error("파일 복사 검증 실패: {}", destinationKey);
                throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
            }
            
        } catch (Exception e) {
            log.error("S3 파일 이동 실패: {} -> {}", sourceKey, destinationKey, e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // 파일 존재 확인
    private boolean verifyFileExists(String s3Key) {
        try {
            s3Client.headObject(builder -> builder.bucket(bucketName).key(s3Key));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 다중 파일 이동
    public void moveFiles(List<String> sourceKeys, List<String> destinationKeys) {
        if (sourceKeys.size() != destinationKeys.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        for (int i = 0; i < sourceKeys.size(); i++) {
            moveFile(sourceKeys.get(i), destinationKeys.get(i));
        }
    }

    // 배치로 파일 삭제
    public void deleteFiles(List<String> s3Keys) {
        for (String s3Key : s3Keys) {
            try {
                deleteFromS3(s3Key);
            } catch (Exception e) {
                log.error("배치 삭제 중 실패한 파일: {}", s3Key, e);
                // 개별 파일 삭제 실패는 로그만 남기고 계속 진행
            }
        }
    }

    // S3 키로부터 Public URL 생성
    public String generatePublicUrl(String s3Key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, s3Key);
    }

    // 현재 S3 설정에 맞는 temp 이미지 URL 패턴 생성
    public String getTempImageUrlPattern() {
        return String.format(
            "https://%s\\.s3\\.%s\\.amazonaws\\.com/users/\\\\d+/temp/[^\\\\s\"'<>]+\\\\.(jpg|jpeg|png|gif|webp)",
            java.util.regex.Pattern.quote(bucketName),
            java.util.regex.Pattern.quote(region)
        );
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