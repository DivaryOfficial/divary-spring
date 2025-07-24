package com.divary.domain.diary.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/logs/{logId}/diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping
    @Operation(
            summary = "일기 생성"
    )
    public ApiResponse<DiaryResponse> createDiary(@Parameter(description = "하나의 log당 하나의 diary가 매핑됩니다. diary 생성시 logId를 보내주세요.") @PathVariable Long logId,  @RequestBody DiaryRequest request) {
        return ApiResponse.success(diaryService.createDiary(logId, request));
    }

//    @PatchMapping
//    @Operation(summary = "일기 수정")
//    public ApiResponse<DiaryResponse> updateDiary(@Parameter(description = "하나의 log당 하나의 diary가 매핑됩니다. diary 수정시 logId를 보내주세요.") @PathVariable Long logId,
//                                                  @ModelAttribute DiaryUpdateRequest request) {
//        return ApiResponse.success(diaryService.updateDiary(logId, request));
//    }
//
    @GetMapping
    @Operation(summary = "일기 조회")
    public ApiResponse<DiaryResponse> getDiary( @Parameter(description = "하나의 log당 하나의 diary가 매핑됩니다. diary 조회시 logId를 보내주세요.", example = "1") @PathVariable Long logId) {
        return ApiResponse.success(diaryService.getDiary(logId));
    }
}
