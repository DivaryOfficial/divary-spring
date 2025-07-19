package com.divary.domain.diary.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.entity.LogBook;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "다이어리 엔티티")
public class Diary extends BaseEntity {

    @Builder
    public Diary(LogBook logBook, String content) {
        this.logBook = logBook;
        this.content = content;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false, unique = true)
    private LogBook logBook;

    @Column(name = "content", nullable = true, length = 255)
    @Schema(description = "일기 본문", example = "오늘은 바다 거북이를 만난 날이다.")
    private String content;

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 폰트, 크기, 손글씨 위치 등)
    public void update(String content) {
        this.content = content;
    }


}
