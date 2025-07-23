package com.divary.domain.encyclopedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "썸네일 이미지 URL", example = "\"https://s3.example.com/card1-thumbnail.jpg\"")
    private String thumbnailUrl;

}
