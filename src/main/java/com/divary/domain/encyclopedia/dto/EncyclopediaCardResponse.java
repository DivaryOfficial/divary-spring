package com.divary.domain.encyclopedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "도감 카드 응답 공통 DTO")
public class EncyclopediaCardResponse {

    @Schema(description = "도감 카드 ID", example = "3")
    private Long id;

    @Schema(description = "이름", example = "갯송사리")
    private String name;

    @Schema(description = "종류", example = "연체동물")
    private String type;

    @Schema(description = "크기", example = "약 1.5~6cm")
    private String size;

    @Schema(description = "출몰 시기", example = "봄, 가을")
    private String appearPeriod;

    @Schema(description = "서식지", example = "따뜻한 연안, 바위 틈")
    private String place;

    @Schema(description = "이미지 URL 목록", example = "[\"...\"]")
    private List<String> imageUrls;
    

    public static EncyclopediaCardResponse summaryOf(com.divary.domain.encyclopedia.entity.EncyclopediaCard card) {
        return EncyclopediaCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .type(card.getType())
                .size(card.getSize())
                .appearPeriod(card.getAppearPeriod())
                .place(card.getPlace())
                .imageUrls(List.of("https://marinepedia.com/images/card" + card.getId() + "-1.jpg"))
                .build();
    }

    public static EncyclopediaCardResponse detailOf(com.divary.domain.encyclopedia.entity.EncyclopediaCard card) {
        return EncyclopediaCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .type(card.getType())
                .size(card.getSize())
                .appearPeriod(card.getAppearPeriod())
                .place(card.getPlace())
                .imageUrls(List.of(
                        "https://s3.example.com/card" + card.getId() + "-img1.jpg",
                        "https://s3.example.com/card" + card.getId() + "-img2.jpg",
                        "https://s3.example.com/card" + card.getId() + "-img3.jpg"
                ))
                .build();
    }
}
