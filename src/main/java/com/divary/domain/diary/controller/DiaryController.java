package com.divary.domain.diary.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.service.DiaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
    ✅ 요청 예시:
    - 텍스트만 입력하는 경우: Send empty value 체크 박스는 누르지 말아주세요. 
    - 사진만 첨부하는 경우: 텍스트가 없으면 Send empty value 체크 박스가 자동으로 선택됩니다. 
    - 둘 다 없는 경우도 허용해두긴 했어요.
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
