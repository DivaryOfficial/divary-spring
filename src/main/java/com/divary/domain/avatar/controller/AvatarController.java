package com.divary.domain.avatar.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.service.AvatarService;
import com.divary.global.config.SwaggerConfig;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/avatar")
public class AvatarController {
    private final AvatarService avatarService;

    @PutMapping
    @Operation(summary = "아바타 저장", description = "아바타를 저장합니다")
    @SwaggerConfig.ApiSuccessResponse(dataType = Void.class)
    @SwaggerConfig.ApiErrorExamples(value = {ErrorCode.AVATAR_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse saveAvatar(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @RequestBody @Valid AvatarRequestDTO avatarRequestDTO) {
        avatarService.upsertAvatar(userPrincipal.getId(), avatarRequestDTO);
        return ApiResponse.success(null);
    }

    @GetMapping
    public  ApiResponse<AvatarResponseDTO> getAvatar(){
        return ApiResponse.success("아바타 조회에 성공했습니다.", avatarService.getAvatar());
    @Operation(summary = "아바타 조회", description = "아바타를 조회합니다")
    @SwaggerConfig.ApiSuccessResponse(dataType = AvatarResponseDTO.class)
    @SwaggerConfig.ApiErrorExamples(value = {ErrorCode.AVATAR_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    public  ApiResponse<AvatarResponseDTO> getAvatar(@AuthenticationPrincipal CustomUserPrincipal userPrincipal){
        return ApiResponse.success("아바타 조회에 성공했습니다.", avatarService.getAvatar(userPrincipal.getId()));
    }
}
