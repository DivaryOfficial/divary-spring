package com.divary.domain.image.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageUploadResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "이미지 업로드 및 관리")
@Slf4j
@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "이미지 업로드", description = "S3에 이미지를 업로드하고 정보를 저장합니다.")
    @ApiErrorExamples({
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file,
            
            @Parameter(description = "이미지 타입", required = true, example = "USER_CHAT")
            @RequestParam("imageType") ImageType imageType,
            
            @Parameter(description = "사용자 ID (USER 타입 이미지의 경우 필수)", example = "1")
            @RequestParam(value = "userId", required = false) Long userId,
            
            @Parameter(description = "날짜 경로 (다이빙 로그 이미지의 경우 필수)", example = "2024/01/15")
            @RequestParam(value = "datePath", required = false) String datePath,
            
            @Parameter(description = "원본 파일명 (선택사항)", example = "diving_photo.jpg")
            @RequestParam(value = "originalFilename", required = false) String originalFilename
    ) {
        ImageUploadRequest request = ImageUploadRequest.builder()
                .file(file)
                .imageType(imageType)
                .userId(userId)
                .datePath(datePath)
                .originalFilename(originalFilename)
                .build();

        ImageUploadResponse response = imageService.uploadImage(request);
        return ApiResponse.success("이미지 업로드가 완료되었습니다.", response);
    }

    @Operation(summary = "사용자별 이미지 목록 조회", description = "특정 사용자가 업로드한 이미지 목록을 조회합니다.")
    @ApiErrorExamples({
            ErrorCode.INVALID_INPUT_VALUE
    })
    @GetMapping("/user/{userId}")
    public ApiResponse<List<Image>> getUserImages(
            @Parameter(description = "사용자 ID", example = "1")
            @PathVariable Long userId
    ) {
        List<Image> images = imageService.getUserImages(userId);
        return ApiResponse.success("사용자 이미지 목록을 조회했습니다.", images);
    }

    @Operation(summary = "타입별 이미지 목록 조회", description = "특정 타입의 이미지 목록을 조회합니다.")
    @ApiErrorExamples({
            ErrorCode.INVALID_INPUT_VALUE
    })
    @GetMapping("/type/{imageType}")
    public ApiResponse<List<Image>> getImagesByType(
            @Parameter(description = "이미지 타입", example = "USER_CHAT")
            @PathVariable String imageType
    ) {
        // 대소문자 무관하게 ImageType으로 변환
        ImageType type = ImageType.valueOf(imageType.toUpperCase());
        List<Image> images = imageService.getImagesByType(type);
        return ApiResponse.success("타입별 이미지 목록을 조회했습니다.", images);
    }

    @Operation(summary = "이미지 삭제", description = "S3와 DB에서 이미지를 삭제합니다.")
    @ApiErrorExamples({
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @DeleteMapping("/{imageId}")
    public ApiResponse<Void> deleteImage(
            @Parameter(description = "이미지 ID", example = "1")
            @PathVariable Long imageId
    ) {
        imageService.deleteImage(imageId);
        return ApiResponse.success("이미지가 삭제되었습니다.", null);
    }

    @Operation(summary = "이미지 상세 조회", description = "이미지 ID로 상세 정보를 조회합니다.")
    @ApiErrorExamples({
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/{imageId}")
    public ApiResponse<Image> getImage(
            @Parameter(description = "이미지 ID", example = "1")
            @PathVariable Long imageId
    ) {
        Image image = imageService.getImageById(imageId);
        return ApiResponse.success("이미지 정보를 조회했습니다.", image);
    }
} 