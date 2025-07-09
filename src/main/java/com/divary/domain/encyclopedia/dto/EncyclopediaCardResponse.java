package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "도감 카드 응답 DTO")
public record EncyclopediaCardResponse(

        @Schema(description = "도감 카드 ID", example = "1")
        Long id,

        @Schema(description = "생물 이름", example = "쥐치")
        String name,

        @Schema(description = "생물 종류", example = "어류")
        String type,

        @Schema(description = "크기", example = "30cm")
        String size,

        @Schema(description = "출몰 시기", example = "여름")
        String appearPeriod,

        @Schema(description = "서식지", example = "연안 암초 지역")
        String place
) {

    public static EncyclopediaCardResponse from(EncyclopediaCard card) {
        return new EncyclopediaCardResponse(
                card.getId(),
                card.getName(),
                card.getType(),
                card.getSize(),
                card.getAppearPeriod(),
                card.getPlace()
        );
    }
}
