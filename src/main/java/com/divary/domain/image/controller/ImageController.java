package com.divary.domain.image.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.dto.response.MultipleImageUploadResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Image", description = "이미지 업로드 및 관리")
@Slf4j
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "임시 이미지 업로드", description = "임시 경로에 다중 이미지를 업로드합니다. 24시간 후 자동 삭제됩니다.")
    @ApiErrorExamples({
            ErrorCode.REQUIRED_FIELD_MISSING,
            ErrorCode.IMAGE_SIZE_TOO_LARGE,
            ErrorCode.IMAGE_FORMAT_NOT_SUPPORTED,
            ErrorCode.AUTHENTICATION_REQUIRED
    })
    @PostMapping(value = "/upload/temp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<MultipleImageUploadResponse> uploadTempImages(
            @Parameter(description = "업로드할 이미지 파일들 (최대 10개)", required = true)
            @RequestPart("files") List<MultipartFile> files,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        MultipleImageUploadResponse response = imageService.uploadTempImages(files, userPrincipal.getId());
        return ApiResponse.success("임시 이미지 업로드가 완료되었습니다. 24시간 내에 사용하지 않으면 자동 삭제됩니다.", response);
    }

    @Operation(summary = "이미지 업로드", description = "S3에 이미지를 업로드하고 정보를 저장합니다.")
    @ApiErrorExamples({
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageResponse> uploadImage(
            @Parameter(description = "업로드할 이미지 파일", required = true,
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file,
            
            @Parameter(description = "S3 업로드 경로", required = true, example = "users/1/chat/10/")
            @RequestParam("uploadPath") String uploadPath) {
        ImageUploadRequest request = ImageUploadRequest.builder()
                .file(file)
                .uploadPath(uploadPath)
                .build();

        ImageResponse response = imageService.uploadImage(request);
        return ApiResponse.success("이미지 업로드가 완료되었습니다.", response);
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
    public ApiResponse<ImageResponse> getImage(
            @Parameter(description = "이미지 ID", example = "1")
            @PathVariable Long imageId
    ) {
        ImageResponse image = imageService.getImageById(imageId);
        return ApiResponse.success("이미지 정보를 조회했습니다.", image);
    }

    @Operation(summary = "타입별 이미지 업로드", description = "ImageType을 기준으로 이미지를 업로드합니다.")
    @ApiErrorExamples({
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @PostMapping(value = "/upload/type/{imageType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageResponse> uploadImageByType(
            @Parameter(description = "이미지 타입", example = "USER_DIVING_LOG")
            @PathVariable ImageType imageType,
            
            @Parameter(description = "업로드할 이미지 파일", required = true,
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestPart("file") MultipartFile file,
            
            @Parameter(description = "사용자 ID (USER 타입의 경우 필수)", example = "1")
            @RequestParam(required = false) Long userId,
            
            @Parameter(description = "추가 경로 (선택사항)", example = "additional/path")
            @RequestParam(required = false) String additionalPath
    ) {
        ImageResponse response = imageService.uploadImageByType(imageType, file, userId, additionalPath);
        return ApiResponse.success("타입별 이미지 업로드가 완료되었습니다.", response);
    }

    @Operation(summary = "이미지 타입별 상세 정보 조회", description = "ImageType을 기준으로 해당 타입의 이미지 상세 정보를 조회합니다.")
    @ApiErrorExamples({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/type/{imageType}")
    public ApiResponse<List<ImageResponse>> getImagesByType(
            @Parameter(description = "이미지 타입", example = "USER_DIVING_LOG")
            @PathVariable ImageType imageType,
            @Parameter(description = "사용자 ID (USER 타입의 경우 필수)", example = "1")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "추가 경로 (선택사항)", example = "additional/path")
            @RequestParam(required = false) String additionalPath
    ) {
        List<ImageResponse> images = imageService.getImagesByType(imageType, userId, additionalPath);
        return ApiResponse.success("이미지 타입별 상세 정보를 조회했습니다.", images);
    }
} 