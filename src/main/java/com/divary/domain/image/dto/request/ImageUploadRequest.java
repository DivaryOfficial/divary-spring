package com.divary.domain.image.dto.request;

import com.divary.domain.image.entity.ImageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이미지 업로드 요청")
public class ImageUploadRequest {
    
    @NotNull(message = "이미지 파일은 필수입니다")
    @Schema(description = "업로드할 이미지 파일")
    private MultipartFile file;
    
    @NotNull(message = "이미지 타입은 필수입니다")
    @Schema(description = "이미지 타입", example = "USER_CHAT")
    private ImageType imageType;
    
    @Schema(description = "사용자 ID (USER 타입 이미지의 경우 필수)", example = "1")
    private Long userId;
    
    @Schema(description = "날짜 경로 (다이빙 로그 이미지의 경우 필수)", example = "2024/01/15")
    private String datePath;
    
    @Schema(description = "원본 파일명", example = "diving_photo.jpg")
    private String originalFilename;
}