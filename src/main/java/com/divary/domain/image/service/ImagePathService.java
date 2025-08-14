package com.divary.domain.image.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.divary.domain.image.enums.ImageType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagePathService {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    
    @Value("${cloud.aws.region.static}")
    private String region;

    // base prefix 방식 사용으로 정규식 캐시 제거
    
    private final ImageValidationService imageValidationService;
        private final ObjectMapper objectMapper;

    // 유저 이미지 업로드 경로 생성
    public String generateUserUploadPath(ImageType imageType, Long userId, String additionalPath) {
        imageValidationService.validateUserImageType(imageType);
        imageValidationService.validateUserId(userId);
        
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
        imageValidationService.validateSystemImageType(imageType);
        
        String typeWithoutPrefix = extractTypeWithoutPrefix(imageType.name(), "SYSTEM_");
        
        StringBuilder path = new StringBuilder("system/")
                .append(typeWithoutPrefix); // system/dogam
        
        if (additionalPath != null && !additionalPath.trim().isEmpty()) {
            appendPath(path, additionalPath);
        }
        
        return path.toString();
    }

    // temp 경로 생성
    public String generateTempPath(Long userId) {
        imageValidationService.validateUserId(userId);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String randomId = UUID.randomUUID().toString().substring(0, 8);
        return String.format("users/%d/temp/%s_%s", userId, timestamp, randomId);
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

    // 본문에서 temp 이미지 URL 패턴 추출
    public List<String> extractTempImageUrls(String content) {
        // JSON 파싱만 사용
        List<String> urls = extractUrlsByJson(content, true);
		log.info("총 발견된 temp URL 개수: {}", urls.size());
		return urls;
    }

    // 컨텐츠에서 모든 이미지 URL 추출 (temp 이미지뿐만 아니라 permanent 이미지도)
    public List<String> extractAllImageUrls(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ArrayList<>();
        }
        // JSON 파싱만 사용
        List<String> urls = extractUrlsByJson(content, false);
		log.debug("컨텐츠에서 추출된 이미지 URL 개수: {}", urls.size());
		return urls;
    }

    // JSON을 파싱하여 문자열 값들 중 base URL로 시작하는 항목을 수집
    private List<String> extractUrlsByJson(String content, boolean tempOnly) {
        List<String> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(content);
            String base = getBaseUrl();
            collectUrlsFromJsonNode(root, base, tempOnly, results);
        } catch (Exception e) {
            log.warn("JSON 파싱 기반 URL 추출 실패: {}", e.getMessage());
        }
        return results;
    }

    private void collectUrlsFromJsonNode(JsonNode node, String base, boolean tempOnly, List<String> results) {
        if (node == null) return;
        if (node.isTextual()) {
            String text = node.asText();
            if (text != null && text.startsWith(base)) {
                if (!tempOnly || text.contains("/temp/")) {
                    results.add(text);
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                collectUrlsFromJsonNode(child, base, tempOnly, results);
            }
            return;
        }
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                collectUrlsFromJsonNode(node.get(name), base, tempOnly, results);
            }
        }
    }

	private String getBaseUrl() {
		return String.format("https://%s.s3.%s.amazonaws.com/", bucketName, region);
	}

    // 본문의 temp URL을 permanent URL로 교체
    public String replaceTempUrls(String content, Map<String, String> urlMappings) {
        String result = content;
        for (Map.Entry<String, String> entry : urlMappings.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        return result;
    }

    // temp 경로 이미지인지 확인
    public boolean isTempImage(String s3Key) {
        return s3Key != null && s3Key.contains("/temp/");
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