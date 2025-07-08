package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.enums.IconType;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Entity
@Getter
@Schema(description = "다이빙 로그")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class LogBook extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "로그 ID", example = "1")
    private Long id;

    @Column(name = "user_id", nullable = false)
    @Schema(description = "사용자 ID", example = "1001")
    private Long userId;

    @Column(name = "name", nullable = false, length = 40)
    @Schema(description = "로그 제목", example = "고래 원정")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon", nullable = false)
    @Schema(description = "아이콘 타입", example = "WHALE")
    private IconType icon;

    @Column(name = "accumulation",nullable = false)
    @Schema(description = "누적 횟수", example = "3")
    private int accumulation;

    @Column(name = "date", nullable = false)
    @Schema(description = "다이빙 날짜", example = "2025-07-25")
    private LocalDateTime date;

}
