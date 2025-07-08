package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.enums.PerceiveWeight;
import com.divary.domain.logbook.enums.SuitType;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "다이빙 장비 정보")
public class Equipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "장비 ID", example = "20")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "logbook_id")
    private LogBook logBook;

    @Enumerated(EnumType.STRING)
    @Column(name = "suit_type")
    @Schema(description = "슈트 종류", example = "WETSUIT_3MM")
    private SuitType suitType;

    @Column(name = "equipment", length = 50)
    @Schema(description = "착용 장비", example = "후드,장갑 등")
    private String equipment;

    @Column(name = "weight")
    @Schema(description = "웨이트(kg)", example = "6")
    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "perceived_weight")
    @Schema(description = "체감 무게", example = "HEAVY")
    private PerceiveWeight perceivedWeight;
}
