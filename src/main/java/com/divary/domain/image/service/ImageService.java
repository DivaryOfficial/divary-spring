package com.divary.domain.image.service;

import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.dto.response.MultipleImageUploadResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
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
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {

    // 상수 정의
    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final int TEMP_IMAGE_EXPIRY_HOURS = 24;
    private static Pattern tempUrlPattern = null;

    private final ImageRepository imageRepository;
    private final ImageStorageService imageStorageService;
    private final ImagePathService imagePathService;

    // 다중 임시 이미지 업로드
    @Transactional
    public MultipleImageUploadResponse uploadTempImages(List<MultipartFile> files, Long userId) {
        validateMultipleFiles(files);
        
        List<ImageResponse> uploadedImages = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                ImageResponse imageResponse = uploadSingleTempImage(file, userId);
                uploadedImages.add(imageResponse);
            } catch (Exception e) {
                log.error("임시 이미지 업로드 실패: {}, 사용자: {}", file.getOriginalFilename(), userId, e);
            }
        }
        
        log.info("임시 이미지 업로드 완료 - 성공: {}/{}, 사용자: {}", 
                uploadedImages.size(), files.size(), userId);
        
        return MultipleImageUploadResponse.of(uploadedImages);
    }

    // 단일 임시 이미지 업로드
    private ImageResponse uploadSingleTempImage(MultipartFile file, Long userId) {
        try {
            ImageMetadata metadata = extractImageMetadata(file);
            
            // temp 경로 생성
            String tempPath = generateTempPath(userId);
            String fileName = imageStorageService.generateUniqueFileName(file.getOriginalFilename());
            String tempS3Key = tempPath + "/" + fileName;
            
            // S3 업로드
            imageStorageService.uploadToS3(tempS3Key, file);
            
            // DB 저장
            Image tempImage = Image.builder()
                    .s3Key(tempS3Key)
                    .type(null)
                    .originalFilename(file.getOriginalFilename())
                    .width(metadata.width)
                    .height(metadata.height)
                    .userId(userId)
                    .build();
            
            Image savedImage = imageRepository.save(tempImage);
            String tempUrl = imageStorageService.generatePublicUrl(tempS3Key);
            
            return ImageResponse.from(savedImage, tempUrl);
            
        } catch (IOException e) {
            log.error("이미지 처리 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // 이미지 업로드 메인 로직
    @Transactional
    public ImageResponse uploadImage(ImageUploadRequest request) {
        validateUploadRequest(request);
        
        try {
            ImageMetadata metadata = extractImageMetadata(request.getFile());
            
            log.debug("이미지 크기 - width: {}, height: {}", metadata.width, metadata.height);
            
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
                    .originalFilename(request.getFile().getOriginalFilename())
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
    public ImageResponse uploadImageByType(ImageType imageType, org.springframework.web.multipart.MultipartFile file, Long userId, String additionalPath) {
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
                .build();
        
        ImageResponse response = uploadImage(request);
        
        Image savedImage = imageRepository.findById(response.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR));
        savedImage.updateType(imageType);
        
        // 업데이트된 이미지로 응답 생성
        return ImageResponse.from(savedImage, response.getFileUrl());
    }

    public List<ImageResponse> getImagesByType(ImageType imageType, Long userId, String additionalPath) {
        // 타입에 따라 적절한 경로 생성
        String uploadPath;
        if (imageType.name().startsWith("USER_")) {
            uploadPath = imagePathService.generateUserUploadPath(imageType, userId, additionalPath);
        } else {
            uploadPath = imagePathService.generateSystemUploadPath(imageType, additionalPath);
        }
        
        List<Image> images = imageRepository.findByS3KeyStartingWith(uploadPath);
        return images.stream()
                .map(image -> ImageResponse.from(image, imageStorageService.generatePublicUrl(image.getS3Key())))
                .collect(Collectors.toList());
    }

    // 본문에서 temp 이미지 URL을 찾아서 permanent URL로 변환
    @Transactional
    public String processContentAndMigrateImages(String content, ImageType imageType, Long userId, String additionalPath) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }
        
        List<String> tempUrls = extractTempImageUrls(content);
        if (tempUrls.isEmpty()) {
            return content;
        }
        
        Map<String, String> urlMappings = new HashMap<>();
        for (String tempUrl : tempUrls) {
            try {
                String permanentUrl = migrateTempImageByUrl(tempUrl, imageType, userId, additionalPath);
                urlMappings.put(tempUrl, permanentUrl);
            } catch (Exception e) {
                log.error("이미지 이동 실패: tempUrl={}, userId={}", tempUrl, userId, e);
            }
        }
        
        return replaceTempUrls(content, urlMappings);
    }

    // 본문에서 temp 이미지 URL 패턴 추출
    private List<String> extractTempImageUrls(String content) {
        if (tempUrlPattern == null) {
            tempUrlPattern = Pattern.compile(imageStorageService.getTempImageUrlPattern(), Pattern.CASE_INSENSITIVE);
        }
        
        Matcher matcher = tempUrlPattern.matcher(content);
        List<String> tempUrls = new ArrayList<>();
        while (matcher.find()) {
            tempUrls.add(matcher.group());
        }
        return tempUrls;
    }

    // temp URL을 permanent URL로 이동 처리
    private String migrateTempImageByUrl(String tempUrl, ImageType imageType, Long userId, String additionalPath) {
        String tempS3Key = extractS3KeyFromUrl(tempUrl);
        
        Image tempImage = imageRepository.findByS3Key(tempS3Key)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
        
        if (!userId.equals(tempImage.getUserId())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        if (isExpiredTempImage(tempImage)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // permanent 경로 생성
        String permanentBasePath = imagePathService.generateUserUploadPath(imageType, userId, additionalPath);
        String newFileName = imageStorageService.generateUniqueFileName(tempImage.getOriginalFilename());
        String newS3Key = imageStorageService.generateS3Key(permanentBasePath, newFileName);
        
        imageStorageService.moveFile(tempS3Key, newS3Key);
        
        tempImage.updateS3Key(newS3Key);
        tempImage.updateType(imageType);
        
        return imageStorageService.generatePublicUrl(newS3Key);
    }

    // 이미지 URL에서 S3 키 추출
    private String extractS3KeyFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split(".amazonaws.com/", 2);
            if (parts.length == 2) {
                return parts[1];
            }
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    // 본문의 temp URL을 permanent URL로 교체
    private String replaceTempUrls(String content, Map<String, String> urlMappings) {
        String result = content;
        for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // temp 경로 이미지인지 확인
    private boolean isTempImage(String s3Key) {
        return s3Key != null && s3Key.contains("/temp/");
    }

    // 만료된 temp 이미지인지 확인
    private boolean isExpiredTempImage(Image image) {
        return isTempImage(image.getS3Key()) && 
               image.getCreatedAt().isBefore(LocalDateTime.now().minusHours(TEMP_IMAGE_EXPIRY_HOURS));
    }

    // 공통 유틸리티 메서드들
    
    // 이미지 메타데이터 추출
    private ImageMetadata extractImageMetadata(MultipartFile file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
        Long width = bufferedImage != null ? (long) bufferedImage.getWidth() : null;
        Long height = bufferedImage != null ? (long) bufferedImage.getHeight() : null;
        return new ImageMetadata(width, height);
    }
    
    // temp 경로 생성
    private String generateTempPath(Long userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("users/%d/temp/%s_%s", userId, timestamp, randomId);
    }
    
    // 다중 파일 검증
    private void validateMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        files.forEach(this::validateImageFile);
    }
    
    // 단일 파일 검증
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new BusinessException(ErrorCode.IMAGE_SIZE_TOO_LARGE);
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.IMAGE_FORMAT_NOT_SUPPORTED);
        }
    }
    
    // 이미지 메타데이터 내부 클래스
    private static class ImageMetadata {
        final Long width;
        final Long height;
        
        ImageMetadata(Long width, Long height) {
            this.width = width;
            this.height = height;
        }
    }

} 