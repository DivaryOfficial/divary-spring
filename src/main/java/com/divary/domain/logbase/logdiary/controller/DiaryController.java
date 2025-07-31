package com.divary.domain.logbase.logdiary.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbase.logdiary.dto.DiaryRequest;
import com.divary.domain.logbase.logdiary.dto.DiaryResponse;
import com.divary.domain.logbase.logdiary.service.DiaryService;
import com.divary.global.config.SwaggerConfig.*;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs/{logBaseInfoId}/diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 생성")
    @ApiSuccessResponse(dataType = DiaryResponse.class)
    @ApiErrorExamples({
            ErrorCode.DIARY_ALREADY_EXISTS,
            ErrorCode.LOG_NOT_FOUND,
            ErrorCode.INVALID_JSON_FORMAT,
            ErrorCode.DIARY_FORBIDDEN_ACCESS
    })
    public ApiResponse<DiaryResponse> createDiary(
            @Parameter(description = "하나의 logBaseInfo당 하나의 diary가 매핑됩니다. diary 생성시 logBaseInfoId를 보내주세요.") @PathVariable  Long logBaseInfoId,
            @RequestBody DiaryRequest request, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        return ApiResponse.success(diaryService.createDiary(userPrincipal.getId(), logBaseInfoId, request));
    }

    @PutMapping
    @Operation(summary = "일기 수정")
    @ApiSuccessResponse(dataType = DiaryResponse.class)
    @ApiErrorExamples({
            ErrorCode.DIARY_NOT_FOUND,
            ErrorCode.INVALID_JSON_FORMAT,
            ErrorCode.DIARY_FORBIDDEN_ACCESS
    })
    public ApiResponse<DiaryResponse> updateDiary(
            @Parameter(description = "하나의 logBaseInfo당 하나의 diary가 매핑됩니다. diary 생성시 logBaseInfoId를 보내주세요.") @PathVariable  Long logBaseInfoId,
            @RequestBody DiaryRequest request, @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        return ApiResponse.success(diaryService.updateDiary(userPrincipal.getId(), logBaseInfoId, request));
    }

    @GetMapping
    @Operation(summary = "일기 조회")
    @ApiSuccessResponse(dataType = DiaryResponse.class)
    @ApiErrorExamples({
            ErrorCode.DIARY_NOT_FOUND,
            ErrorCode.DIARY_FORBIDDEN_ACCESS
    })
    public ApiResponse<DiaryResponse> getDiary(
            @Parameter(description = "하나의 logBaseInfo당 하나의 diary가 매핑됩니다. diary 생성시 logBaseInfoId를 보내주세요.") @PathVariable  Long logBaseInfoId,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        return ApiResponse.success(diaryService.getDiary(userPrincipal.getId(), logBaseInfoId));
    }
}