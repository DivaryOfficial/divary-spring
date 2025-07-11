package com.divary.domain.image.dto.response;

import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 업로드 응답")
public class ImageUploadResponse {
    
    @Schema(description = "이미지 ID", example = "1")
    private Long id;
    
    @Schema(description = "이미지 파일 URL", example = "https://s3.amazonaws.com/bucket/users/1/chat/image.jpg")
    private String fileUrl;
    
    @Schema(description = "원본 파일명", example = "diving_photo.jpg")
    private String originalFilename;
    
    @Schema(description = "이미지 너비", example = "1920")
    private Long width;
    
    @Schema(description = "이미지 높이", example = "1080")
    private Long height;
    
    @Schema(description = "이미지 타입", example = "USER_CHAT")
    private ImageType imageType;
    
    @Schema(description = "업로드 일시", example = "2024-01-15T10:30:00")
    private LocalDateTime uploadedAt;
    
    @Schema(description = "사용자 ID", example = "1")
    private Long userId;
    
    public static ImageUploadResponse from(Image image, String fileUrl) {
        return ImageUploadResponse.builder()
                .id(image.getId())
                .fileUrl(fileUrl)
                .originalFilename(image.getOriginalFilename())
                .width(image.getWidth())
                .height(image.getHeight())
                .imageType(image.getType())
                .uploadedAt(image.getCreatedAt())
                .userId(image.getUserId())
                .build();
    }
}