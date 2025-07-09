package com.divary.domain.encyclopedia.entity;

import com.divary.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "significant")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "생물특이사항")
public class Significant extends BaseEntity {

    @Schema(description = "독성 여부", example = "무독성")
    private String toxicity;

    @Schema(description = "생존 전략", example = "체색 변화로 위장")
    private String strategy;

    @Schema(description = "관찰 팁", example = "조용히 접근하면 모습을 드러냄")
    private String observeTip;

    @Schema(description = "기타 특성", example = "야행성")
    private String otherFeature;

    @OneToOne
    @JoinColumn(name = "card_id", nullable = false, unique = true)
    private EncyclopediaCard card;
}
