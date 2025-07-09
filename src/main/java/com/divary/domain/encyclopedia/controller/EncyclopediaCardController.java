package com.divary.domain.encyclopedia.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.service.EncyclopediaCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class EncyclopediaCardController {

    private final EncyclopediaCardService encyclopediaCardService;

    @GetMapping("/{cardId}/summary")
    public ApiResponse<EncyclopediaCardSummaryResponse> getSummary(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getSummary(cardId));
    }
}
