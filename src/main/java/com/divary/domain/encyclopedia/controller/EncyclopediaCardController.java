package com.divary.domain.encyclopedia.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
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
    public ApiResponse<List<EncyclopediaCardResponse>> getCards(@RequestParam(required = false) String type) {
        return ApiResponse.success(encyclopediaCardService.getCards(type));
    }

    @GetMapping("/{cardId}/summary")
    public ApiResponse<EncyclopediaCardResponse> getSummary(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getSummary(cardId));
    }

    @GetMapping("/{cardId}/detail")
    public ApiResponse<EncyclopediaCardResponse> getDetail(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getDetail(cardId));
    }

    @GetMapping("/{cardId}/appearance")
    public ApiResponse<AppearanceResponse> getAppearance(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getAppearance(cardId));
    }

    @GetMapping("/{cardId}/personality")
    public ApiResponse<PersonalityResponse> getPersonality(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getPersonality(cardId));
    }

    @GetMapping("/{cardId}/significant")
    public ApiResponse<SignificantResponse> getSignificant(@PathVariable Long cardId) {
        return ApiResponse.success(encyclopediaCardService.getSignificant(cardId));
    }

}
