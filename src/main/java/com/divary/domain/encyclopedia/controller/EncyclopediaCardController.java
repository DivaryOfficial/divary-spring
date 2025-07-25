package com.divary.domain.encyclopedia.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.dto.PersonalityResponse;
import com.divary.domain.encyclopedia.dto.SignificantResponse;
import com.divary.domain.encyclopedia.service.EncyclopediaCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class EncyclopediaCardController {

    private final EncyclopediaCardService encyclopediaCardService;

    @GetMapping
    @Operation(summary = "도감 카드 리스트 조회")
    public ApiResponse<List<EncyclopediaCardSummaryResponse>> getCards(
            @RequestParam(required = false) @Parameter(description = "생물 종류 필터 (어류, 갑각류, 연체동물, 기타). 미입력 시 전체 조회") String type) {
        return ApiResponse.success(encyclopediaCardService.getCards(type));
    }

    @GetMapping("/{cardId}")
    @Operation(summary = "도감 카드 상세 조회")
    public ApiResponse<EncyclopediaCardResponse> getDetail(
            @Parameter(description = "도감 카드의 고유 ID") @PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getDetail(cardId));
    }
}
