package com.divary.domain.image.service;

import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.dto.response.MultipleImageUploadResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.repository.ImageRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TempImageService {
    
    private final ImageRepository imageRepository;
    private final ImageStorageService imageStorageService;
    private static final int MAX_FILES_PER_UPLOAD = 10;
    
    @Transactional
    public MultipleImageUploadResponse uploadTempImages(List<MultipartFile> files, Long userId) {
        validateUploadRequest(files);
        
        List<ImageResponse> uploadedImages = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                ImageResponse imageResponse = uploadSingleTempImage(file, userId);
                uploadedImages.add(imageResponse);
            } catch (Exception e) {
                log.error("임시 이미지 업로드 실패: {}, 사용자: {}", file.getOriginalFilename(), userId, e);
                // 실패한 이미지는 건너뛰고 계속 진행
            }
        }
        
        log.info("임시 이미지 업로드 완료 - 성공: {}/{}, 사용자: {}", 
                uploadedImages.size(), files.size(), userId);
        
        return MultipleImageUploadResponse.of(uploadedImages);
    }
    
    private ImageResponse uploadSingleTempImage(MultipartFile file, Long userId) {
        try {
            // 이미지 크기 추출
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            Long width = bufferedImage != null ? (long) bufferedImage.getWidth() : null;
            Long height = bufferedImage != null ? (long) bufferedImage.getHeight() : null;
            
            // temp 경로 생성: users/1/temp/20250724_143022_abc12345/
            String tempPath = generateTempPath(userId);
            String fileName = imageStorageService.generateUniqueFileName(file.getOriginalFilename());
            String tempS3Key = tempPath + "/" + fileName;
            
            // S3 업로드
            imageStorageService.uploadToS3(tempS3Key, file);
            
            // DB 저장 (temp 경로에 저장됨)
            Image tempImage = Image.builder()
                    .s3Key(tempS3Key)
                    .type(null) // 타입은 나중에 확정 시점에 설정
                    .originalFilename(file.getOriginalFilename())
                    .width(width)
                    .height(height)
                    .userId(userId)
                    .build();
            
            Image savedImage = imageRepository.save(tempImage);
            String tempUrl = imageStorageService.generatePublicUrl(tempS3Key);
            
            log.debug("임시 이미지 업로드 성공: {}, S3 Key: {}", file.getOriginalFilename(), tempS3Key);
            
            return ImageResponse.from(savedImage, tempUrl);
            
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    
    private String generateTempPath(Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("users/%d/temp/%s_%s", userId, timestamp, randomId);
    }
    
    private void validateUploadRequest(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        for (MultipartFile file : files) {
            validateSingleFile(file);
        }
    }
    
    private void validateSingleFile(MultipartFile file) {
        // 빈 파일 검증
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        // 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.IMAGE_SIZE_TOO_LARGE);
        }
        
        // 파일 타입 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_NOT_SUPPORTED);
        }
    }
    
    // temp 파일인지 확인
    public boolean isTempImage(String s3Key) {
        return s3Key != null && s3Key.contains("/temp/");
    }
    
    // 만료된 temp 파일인지 확인 (24시간 기준)
    public boolean isExpiredTempImage(Image image) {
        if (!isTempImage(image.getS3Key())) {
            return false;
        }
        return image.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24));
    }
}