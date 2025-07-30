package com.divary.domain.image.service;

import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageValidationService {

    private static final int MAX_FILES_PER_UPLOAD = 10;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10MB

    public void validateUploadRequest(ImageUploadRequest request) {
        validateFile(request.getFile());
        validateUploadPath(request.getUploadPath());
    }

    public void validateMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.REQUIRED_FIELD_MISSING);
        }
        
        if (files.size() > MAX_FILES_PER_UPLOAD) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        files.forEach(this::validateFile);
    }

    public void validateFile(MultipartFile file) {
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

    private void validateUploadPath(String uploadPath) {
        if (uploadPath == null || uploadPath.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
        
        // 경로 보안 검증 (../ 등 상위 디렉토리 접근 차단)
        if (uploadPath.contains("..") || uploadPath.startsWith("/")) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }
    }
}