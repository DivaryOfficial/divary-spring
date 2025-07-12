package com.divary.domain.image.service;

import com.divary.domain.image.entity.ImageType;
import org.springframework.stereotype.Service;

@Service
// 이미지 경로 생성 서비스
public class ImagePathService {

    // 유저 이미지 업로드 경로 생성
    public String generateUserUploadPath(ImageType imageType, Long userId, String additionalPath) {
        validateUserImageType(imageType);
        validateUserId(userId);
        
        String typeWithoutPrefix = extractTypeWithoutPrefix(imageType.name(), "USER_");
        
        StringBuilder path = new StringBuilder("users/")
                .append(userId)
                .append("/")
                .append(typeWithoutPrefix);
        
        if (additionalPath != null && !additionalPath.trim().isEmpty()) {
            appendPath(path, additionalPath);
        }
        
        return path.toString();
    }
    
    // 시스템 이미지 업로드 경로 생성
    public String generateSystemUploadPath(ImageType imageType, String additionalPath) {
        validateSystemImageType(imageType);
        
        String typeWithoutPrefix = extractTypeWithoutPrefix(imageType.name(), "SYSTEM_");
        
        StringBuilder path = new StringBuilder("system/")
                .append(typeWithoutPrefix);
        
        if (additionalPath != null && !additionalPath.trim().isEmpty()) {
            appendPath(path, additionalPath);
        }
        
        return path.toString();
    }

    // 경로에서 사용자 ID 추출
    public Long extractUserIdFromPath(String uploadPath) {
        if (uploadPath != null && uploadPath.startsWith("users/")) {
            String[] parts = uploadPath.split("/");
            if (parts.length >= 2) {
                try {
                    return Long.parseLong(parts[1]);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void validateUserImageType(ImageType imageType) {
        if (!imageType.name().startsWith("USER_")) {
            throw new IllegalArgumentException("USER 타입 이미지만 처리 가능합니다: " + imageType);
        }
    }

    private void validateSystemImageType(ImageType imageType) {
        if (!imageType.name().startsWith("SYSTEM_")) {
            throw new IllegalArgumentException("SYSTEM 타입 이미지만 처리 가능합니다: " + imageType);
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 필수입니다.");
        }
    }

    private String extractTypeWithoutPrefix(String typeName, String prefix) {
        return typeName.substring(prefix.length()).toLowerCase();
    }

    private void appendPath(StringBuilder path, String additionalPath) {
        if (!additionalPath.startsWith("/")) {
            path.append("/");
        }
        path.append(additionalPath);
    }
} 