package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.enums.DivePurpose;
import com.divary.domain.logbook.enums.DiveMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Schema(description = "로그의 다이빙개요")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Overview extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "개요 ID", example = "10")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "logbook_id", nullable = false)
    private LogBook logBook;

    @Column(name = "place",length = 50)
    @Schema(description = "다이빙 지역", example = "제주도 서귀포시")
    private String place;

    @Column(name = "dive_point",length = 50)
    @Schema(description = "다이빙 포인트", example = "문섬")
    private String divePoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "dive_type")
    @Schema(description = "다이빙 방식", example = "보트")
    private DiveMethod diveMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "dive_purpose")
    @Schema(description = "다이빙 목적", example = "펀 다이빙")
    private DivePurpose divePurpose;
}
