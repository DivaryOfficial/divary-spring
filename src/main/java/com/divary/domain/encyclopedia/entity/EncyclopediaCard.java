package com.divary.domain.encyclopedia.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.encyclopedia.enums.Type;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "생물 종류", example = "어류")
    private Type type;

    @Schema(description = "생물 크기", example = "30cm")
    private String size;

    @Schema(description = "출몰 시기", example = "여름")
    private String appearPeriod;

    @Schema(description = "서식지", example = "연안 암초 지역")
    private String place;

    @ElementCollection
    @CollectionTable(name = "encyclopedia_card_image_urls", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Embedded
    @Schema(description = "외모 값 객체")
    private Appearance appearance;

    @Embedded
    @Schema(description = "성격 값 객체")
    private Personality personality;

    @Embedded
    @Schema(description = "특이사항 값 객체")
    private Significant significant;
}
