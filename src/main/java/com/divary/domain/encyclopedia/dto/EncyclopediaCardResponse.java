package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.enums.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "도감 카드 상세 응답")
public class EncyclopediaCardResponse {

    @Schema(description = "도감 카드 ID", example = "3")
    private Long id;

    @Schema(description = "이름", example = "갯송사리")
    private String name;

    @Schema(description = "종류", example = "어류")
    private String type;

    @Schema(description = "크기", example = "약 1.5~6cm")
    private String size;

    @Schema(description = "출몰 시기", example = "봄, 가을")
    private String appearPeriod;

    @Schema(description = "서식지", example = "따뜻한 연안, 바위 틈")
    private String place;

    @Schema(
            description = "이미지 URL 목록",
            example = "[\"https://s3.example.com/card1-img1.jpg\", \"https://s3.example.com/card1-img2.jpg\"]"
    )
    private List<String> imageUrls;

    @Schema(description = "외모 정보")
    private AppearanceResponse appearance;

    @Schema(description = "성격 정보")
    private PersonalityResponse personality;

    @Schema(description = "특이사항 정보")
    private SignificantResponse significant;

    public static EncyclopediaCardResponse from(
            com.divary.domain.encyclopedia.entity.EncyclopediaCard card) {

        return EncyclopediaCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .type(card.getType().getDescription())
                .size(card.getSize())
                .appearPeriod(card.getAppearPeriod())
                .place(card.getPlace())
                .imageUrls(card.getImageUrls())
                .appearance(AppearanceResponse.from(card.getAppearance()))
                .personality(PersonalityResponse.from(card.getPersonality()))
                .significant(SignificantResponse.from(card.getSignificant()))
                .build();
    }

}
