package com.divary.domain.encyclopedia.entity;

import com.divary.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "encyclopedia_card")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "도감 카드")
public class EncyclopediaCard extends BaseEntity {

    @Schema(description = "생물 이름", example = "쥐치")
    private String name;

    @Schema(description = "생물 종류", example = "어류")
    private String type;

    @Schema(description = "생물 크기", example = "30cm")
    private String size;

    @Schema(description = "출몰 시기", example = "여름")
    private String appearPeriod;

    @Schema(description = "서식지", example = "연안 암초 지역")
    private String place;

    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Appearance appearance;

    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Personality personality;

    @OneToOne(mappedBy = "card", cascade = CascadeType.ALL, orphanRemoval = true)
    private Significant significant;
}
