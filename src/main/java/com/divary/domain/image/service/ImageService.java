package com.divary.domain.image.service;

import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.repository.ImageRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    private final ImageRepository imageRepository;
    private final ImageStorageService imageStorageService;
    private final ImagePathService imagePathService;

    // 이미지 업로드 메인 로직
    @Transactional
    public ImageResponse uploadImage(ImageUploadRequest request) {
        validateUploadRequest(request);
        
        try {
            // 이미지 크기 추출
            BufferedImage bufferedImage = ImageIO.read(request.getFile().getInputStream());
            Long width = bufferedImage != null ? (long) bufferedImage.getWidth() : null;
            Long height = bufferedImage != null ? (long) bufferedImage.getHeight() : null;
            
            // 파일명 생성
            String fileName = imageStorageService.generateUniqueFileName(request.getFile().getOriginalFilename());
            
            // S3 키 생성
            String s3Key = imageStorageService.generateS3Key(request.getUploadPath(), fileName);
            
            // S3에 업로드
            imageStorageService.uploadToS3(s3Key, request.getFile());
            
            // DB에 저장
            Image image = Image.builder()
                    .s3Key(s3Key)
                    .type(null)
                    .originalFilename(request.getOriginalFilename() != null ? 
                                    request.getOriginalFilename() : request.getFile().getOriginalFilename())
                    .width(width)
                    .height(height)
                    .userId(imagePathService.extractUserIdFromPath(request.getUploadPath()))
                    .build();
            
            Image savedImage = imageRepository.save(image);
            
            String fileUrl = imageStorageService.generatePublicUrl(s3Key);
            return ImageResponse.from(savedImage, fileUrl);
            
        } catch (IOException e) {
            log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 경로 패턴으로 이미지 목록 조회
    public List<ImageResponse> getImagesByPath(String pathPattern) {
        List<Image> images = imageRepository.findByS3KeyStartingWith(pathPattern);
        return images.stream()
                .map(image -> ImageResponse.from(image, imageStorageService.generatePublicUrl(image.getS3Key())))
                .collect(Collectors.toList());
    }

    // 이미지 상세 조회 (URL 포함)
    public ImageResponse getImageById(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        
        String fileUrl = imageStorageService.generatePublicUrl(image.getS3Key());
        return ImageResponse.from(image, fileUrl);
    }
    
    // 이미지 삭제
    @Transactional
    public void deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        
        // S3에서 삭제
        imageStorageService.deleteFromS3(image.getS3Key());
        
        // DB에서 삭제
        imageRepository.delete(image);
    }

    private void validateUploadRequest(ImageUploadRequest request) {
        // 파일 검증
        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 업로드 경로 검증
        if (request.getUploadPath() == null || request.getUploadPath().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 경로 보안 검증 (../ 등 상위 디렉토리 접근 차단)
        if (request.getUploadPath().contains("..") || request.getUploadPath().startsWith("/")) {
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

    // 타입별 이미지 업로드
    @Transactional
    public ImageResponse uploadImageByType(ImageType imageType, org.springframework.web.multipart.MultipartFile file, Long userId, String additionalPath, String originalFilename) {
        // 업로드 경로 생성
        String uploadPath;
        if (imageType.name().startsWith("USER_")) {
            uploadPath = imagePathService.generateUserUploadPath(imageType, userId, additionalPath);
        } else {
            uploadPath = imagePathService.generateSystemUploadPath(imageType, additionalPath);
        }
        
        ImageUploadRequest request = ImageUploadRequest.builder()
                .file(file)
                .uploadPath(uploadPath)
                .originalFilename(originalFilename)
                .build();
        
        ImageResponse response = uploadImage(request);
        
        Image savedImage = imageRepository.findById(response.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        savedImage.updateType(imageType);
        
        // 업데이트된 이미지로 응답 생성
        return ImageResponse.from(savedImage, response.getFileUrl());
    }

    public List<ImageResponse> getImagesByType(ImageType imageType, Long userId, String additionalPath) {
        String uploadPath = imagePathService.generateUserUploadPath(imageType, userId, additionalPath);
        List<Image> images = imageRepository.findByS3KeyStartingWith(uploadPath);
        return images.stream()
                .map(image -> ImageResponse.from(image, imageStorageService.generatePublicUrl(image.getS3Key())))
                .collect(Collectors.toList());
    }

} 