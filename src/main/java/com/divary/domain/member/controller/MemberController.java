package com.divary.domain.member.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.global.config.SwaggerConfig;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.divary.domain.member.service.MemberService;

@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PatchMapping("/level")
    @SwaggerConfig.ApiSuccessResponse(dataType = Void.class)
    @SwaggerConfig.ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse updateLevel(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @Valid @RequestBody MyPageLevelRequestDTO requestDTO) {
        memberService.updateLevel(userPrincipal.getId(), requestDTO);
        return ApiResponse.success(null);
    }

}
