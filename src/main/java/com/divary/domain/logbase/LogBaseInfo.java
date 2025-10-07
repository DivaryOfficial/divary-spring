package com.divary.domain.logbase;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.member.entity.Member;
import com.divary.domain.logbase.logbook.entity.LogBook;
import com.divary.domain.logbase.logbook.enums.IconType;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logdiary.entity.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Schema(description = "다이빙 로그 기본정보")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Setter
public class LogBaseInfo extends BaseEntity {
    @OneToOne(mappedBy = "logBaseInfo", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @Schema(description = "유저 id", example = "1L")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @OneToMany(mappedBy = "logBaseInfo", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<LogBook> logBooks = new ArrayList<>();

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
    @Builder.Default
    private SaveStatus saveStatus = SaveStatus.COMPLETE;

    public void updateName(String name) {
        this.name = name;
    }
}
