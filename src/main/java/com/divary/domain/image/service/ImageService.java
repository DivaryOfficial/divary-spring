package com.divary.domain.image.service;

import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageUploadResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.repository.ImageRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;
    
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 이미지 업로드
     */
    @Transactional
    public ImageUploadResponse uploadImage(ImageUploadRequest request) {
        validateUploadRequest(request);
        
        try {
            // 1. 파일명 생성
            String fileName = generateFileName(request.getFile().getOriginalFilename());
            
            // 2. S3 키 생성
            String s3Key = generateS3Key(request.getImageType(), request.getUserId(), 
                                        request.getDatePath(), fileName);
            
            // 3. S3에 업로드
            uploadToS3(s3Key, request.getFile());
            
            // 4. 이미지 크기 추출
            BufferedImage bufferedImage = ImageIO.read(request.getFile().getInputStream());
            Long width = bufferedImage != null ? (long) bufferedImage.getWidth() : null;
            Long height = bufferedImage != null ? (long) bufferedImage.getHeight() : null;
            
            // 5. DB에 저장
            Image image = Image.builder()
                    .s3Key(s3Key)
                    .type(request.getImageType())
                    .originalFilename(request.getOriginalFilename() != null ? 
                                    request.getOriginalFilename() : request.getFile().getOriginalFilename())
                    .width(width)
                    .height(height)
                    .userId(request.getUserId())
                    .build();
            
            Image savedImage = imageRepository.save(image);
            
            // 6. 응답 생성
            String fileUrl = generateFileUrl(s3Key);
            return ImageUploadResponse.from(savedImage, fileUrl);
            
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 사용자의 이미지 목록 조회
     */
    public List<Image> getUserImages(Long userId) {
        return imageRepository.findByUserId(userId);
    }

    /**
     * 타입별 이미지 목록 조회
     */
    public List<Image> getImagesByType(ImageType type) {
        return imageRepository.findByType(type);
    }

    /**
     * 이미지 상세 조회
     */
    public Image getImageById(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /**
     * 이미지 삭제
     */
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        
        try {
            // S3에서 삭제
            s3Client.deleteObject(builder -> builder.bucket(bucketName).key(image.getS3Key()));
            
            // DB에서 삭제
            imageRepository.delete(image);
            
        } catch (Exception e) {
            log.error("이미지 삭제 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private void validateUploadRequest(ImageUploadRequest request) {
        // 파일 검증
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 이미지 타입 검증
        if (request.getImageType() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // USER 타입 이미지의 경우 userId 필수
        if (request.getImageType().isRequiresUserId() && request.getUserId() == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 다이빙 로그 이미지의 경우 datePath 필수
        if (request.getImageType() == ImageType.USER_DIVING_LOG && 
            (request.getDatePath() == null || request.getDatePath().trim().isEmpty())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 파일 크기 검증 (10MB 제한)
        if (request.getFile().getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 파일 타입 검증
        String contentType = request.getFile().getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }

    private String generateFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String extension = getFileExtension(originalFilename);
        return uuid + (extension.isEmpty() ? "" : "." + extension);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateS3Key(ImageType imageType, Long userId, String datePath, String fileName) {
        String pathPattern = imageType.getPathPattern();
        
        // 플레이스홀더 치환
        String s3Key = pathPattern.replace("{fileName}", fileName);
        
        if (userId != null) {
            s3Key = s3Key.replace("{userId}", userId.toString());
        }
        
        if (datePath != null) {
            s3Key = s3Key.replace("{datePath}", datePath);
        }
        
        if (imageType == ImageType.SYSTEM_DOGAM) {
            // type 플레이스홀더는 별도 처리 필요 (향후 확장)
            s3Key = s3Key.replace("{type}", "default");
        }
        
        return s3Key;
    }

    private void uploadToS3(String s3Key, MultipartFile file) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .acl(ObjectCannedACL.PUBLIC_READ)  // public 읽기 권한 설정
                .build();

        s3Client.putObject(putObjectRequest, 
                          RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        
        log.info("S3 업로드 완료 (public-read): {}", s3Key);
    }

    private String generateFileUrl(String s3Key) {
        // S3 public URL 생성
        return String.format("https://%s.s3.ap-northeast-2.amazonaws.com/%s", bucketName, s3Key);
    }
} 