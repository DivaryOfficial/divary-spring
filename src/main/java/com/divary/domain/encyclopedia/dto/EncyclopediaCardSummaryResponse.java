package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "도감 카드 요약 응답 DTO")
public record EncyclopediaCardSummaryResponse(

        @Schema(description = "도감 카드 ID", example = "3")
        Long id,

        @Schema(description = "생물 이름", example = "갯송사리")
        String name,

        @Schema(description = "생물 종류", example = "연체동물")
        String type,

        @Schema(description = "크기", example = "약 1.5~6cm")
        String size,

        @Schema(description = "출몰 시기", example = "봄, 가을")
        String appearPeriod,

        @Schema(description = "서식지", example = "따뜻한 연안, 바위 틈")
        String place,

        @Schema(description = "이미지 URL 목록", example = "[\"https://marinepedia.com/images/card3-1.jpg\"]")
        List<String> imageUrl
) {
    public static EncyclopediaCardSummaryResponse from(EncyclopediaCard card) {
        return new EncyclopediaCardSummaryResponse(
                card.getId(),
                card.getName(),
                card.getType(),
                card.getSize(),
                card.getAppearPeriod(),
                card.getPlace(),
                // TODO: 이후 이미지 테이블과 연관되면 실제 이미지 URL 리스트로 교체 예정
                List.of("https://marinepedia.com/images/card" + card.getId() + "-1.jpg") // 예시
        );
    }
}
