package com.divary.domain.encyclopedia.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.dto.PersonalityResponse;
import com.divary.domain.encyclopedia.dto.SignificantResponse;
import com.divary.domain.encyclopedia.service.EncyclopediaCardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class EncyclopediaCardController {

    private final EncyclopediaCardService encyclopediaCardService;

    @GetMapping
    public ApiResponse<List<EncyclopediaCardSummaryResponse>> getCards(@RequestParam(required = false) String type) {
        return ApiResponse.success(encyclopediaCardService.getCards(type));
    }

    @GetMapping("/{cardId}")
    public ApiResponse<EncyclopediaCardResponse> getDetail(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getDetail(cardId));
    }

}
