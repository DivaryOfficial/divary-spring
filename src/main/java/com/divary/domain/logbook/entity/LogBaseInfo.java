package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.logbook.enums.IconType;
import com.divary.domain.logbook.enums.SaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Schema(description = "다이빙 로그 기본정보")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Setter
public class LogBaseInfo extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "유저 id", example = "1L")
    private Member member;

    @Column(name = "name", nullable = false, length = 40)
    @Schema(description = "로그 제목", example = "고래 원정")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_type", nullable = false)
    @Schema(description = "아이콘 타입", example = "WHALE")
    private IconType iconType;

    @Column(name = "date", nullable = false)
    @Schema(description = "다이빙 날짜", example = "2025-07-25")
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "save_status",nullable = false)
    @Schema(description = "저장 상태", example = "COMPLETE")
    private SaveStatus saveStatus = SaveStatus.COMPLETE;

    public void updateName(String name) {
        this.name = name;
    }
}
