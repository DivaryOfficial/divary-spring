package com.divary.domain.encyclopedia.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.service.EncyclopediaCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class EncyclopediaCardController {

    private final EncyclopediaCardService encyclopediaCardService;

    @GetMapping("/{cardId}/summary")
    public ApiResponse<EncyclopediaCardResponse> getSummary(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getSummary(cardId));
    }

    @GetMapping("/{cardId}/detail")
    public ApiResponse<EncyclopediaCardResponse> getDetail(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getDetail(cardId));
    }
}
