package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도감 카드 썸네일 응답 DTO")
public class EncyclopediaCardSummaryResponse {

    @Schema(description = "도감 카드 ID", example = "1")
    private Long id;

    @Schema(description = "생물 이름", example = "흰동가리")
    private String name;

    @Schema(description = "생물 종류", example = "어류")
    private String type;

    @Schema(description = "썸네일 이미지 URL 목록", example = "[\"https://s3.example.com/card1-thumbnail.jpg\"]")
    private List<String> imageUrls;

    public static EncyclopediaCardSummaryResponse from(EncyclopediaCard card) {
        return EncyclopediaCardSummaryResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .type(card.getType().getDescription())
                .imageUrls(
                        card.getThumbnail() != null
                                ? Collections.singletonList(card.getThumbnail().getS3Key())
                                : Collections.emptyList()
                )
                .build();
    }
}
