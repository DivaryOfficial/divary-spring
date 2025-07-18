package com.divary.domain.diary.controller;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.service.DiaryService;
import com.divary.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/logs/{logId}/diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(summary = "일기 생성")
    public ApiResponse<Void> createDiary(@PathVariable Long logId, @ModelAttribute DiaryRequest request) {
        diaryService.createDiary(logId, request);
        return ApiResponse.success(null);
    }

    @PatchMapping
    @Operation(summary = "일기 수정")
    public ApiResponse<Void> updateDiary(@PathVariable Long logId, @ModelAttribute DiaryUpdateRequest request) {
        diaryService.updateDiary(logId, request);
        return ApiResponse.success(null);
    }

    @GetMapping
    @Operation(summary = "일기 조회")
    public ApiResponse<DiaryResponse> getDiary(@PathVariable Long logId) {
        return ApiResponse.success(diaryService.getDiary(logId));
    }
}
