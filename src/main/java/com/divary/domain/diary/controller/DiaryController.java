package com.divary.domain.diary.controller;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.service.DiaryService;
import com.divary.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/logs/{logId}/diary")
@RequiredArgsConstructor
@Tag(name = "Diary", description = "일기 API")
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "일기 생성 (텍스트 또는 이미지)",
            description = """
    📌 multipart/form-data 형식으로 content(텍스트)와 images(사진)를 함께 전송합니다.

    content와 images는 모두 선택 값(optional)입니다.  
    사용자는 텍스트만 입력하거나, 사진만 첨부하거나, 또는 둘 다 입력하지 않을 수도 있습니다.  

    ✅ 요청 예시:
    - 텍스트만 입력하는 경우: images 필드 생략
    - 사진만 첨부하는 경우: content 필드 생략
    - 둘 다 없는 경우도 허용
    """
    )
    public ApiResponse<DiaryResponse> createDiary(@PathVariable Long logId, @ModelAttribute DiaryRequest request) {
        return ApiResponse.success(diaryService.createDiary(logId, request));
    }

    @PatchMapping
    @Operation(summary = "일기 수정")
    public ApiResponse<DiaryResponse> updateDiary(@PathVariable Long logId,
                                                  @ModelAttribute DiaryUpdateRequest request) {
        return ApiResponse.success(diaryService.updateDiary(logId, request));
    }

    @GetMapping
    @Operation(summary = "일기 조회")
    public ApiResponse<DiaryResponse> getDiary(@PathVariable Long logId) {
        return ApiResponse.success(diaryService.getDiary(logId));
    }
}
