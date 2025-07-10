package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.embedded.Significant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도감 생물 특이사항 응답 DTO")
public class SignificantResponse {
    @Schema(description = "독성 여부", example = "약한 독성을 지님")
    private String toxicity;

    @Schema(description = "생존 전략", example = "화려한 색으로 위장하거나 경고")
    private String strategy;

    @Schema(description = "관찰 팁", example = "조용히 접근하면 움직임을 관찰할 수 있음")
    private String observeTip;

    @Schema(description = "기타 특성", example = "먹이를 먹을 때 입 주변 촉수가 움직임")
    private String otherFeature;

    public static SignificantResponse from(Significant significant) {
        return SignificantResponse.builder()
                .toxicity(significant.getToxicity())
                .strategy(significant.getStrategy())
                .observeTip(significant.getObserveTip())
                .otherFeature(significant.getOtherFeature())
                .build();
    }
}
