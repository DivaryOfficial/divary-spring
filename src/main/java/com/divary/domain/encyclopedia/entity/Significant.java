package com.divary.domain.encyclopedia.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "생물특이사항")
public class Significant {

    @Schema(description = "독성 여부", example = "무독성")
    private String toxicity;

    @Schema(description = "생존 전략", example = "체색 변화로 위장")
    private String strategy;

    @Schema(description = "관찰 팁", example = "조용히 접근하면 모습을 드러냄")
    private String observeTip;

    @Schema(description = "기타 특성", example = "야행성")
    private String otherFeature;

}
