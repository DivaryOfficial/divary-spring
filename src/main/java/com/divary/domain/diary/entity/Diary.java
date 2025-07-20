package com.divary.domain.diary.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.diary.enums.FontSize;
import com.divary.domain.diary.enums.FontType;
import com.divary.domain.diary.enums.TextAlign;
import com.divary.domain.logbook.entity.LogBook;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
    public Diary(LogBook logBook, String content,
                 FontType fontType, FontSize fontSize,
                 Boolean italic, Boolean underline,
                 Boolean strikethrough, TextAlign textAlign) {
        this.logBook = logBook;
        this.content = content;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.textAlign = textAlign;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_id", nullable = false, unique = true)
    private LogBook logBook;

    @Column(name = "content", nullable = true, length = 255)
    @Schema(description = "일기 본문", example = "오늘은 바다 거북이를 만난 날이다.")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "font_type")
    private FontType fontType;

    @Enumerated(EnumType.STRING)
    @Column(name = "font_size")
    private FontSize fontSize;

    @Column(name = "italic")
    private Boolean italic;

    @Column(name = "underline")
    private Boolean underline;

    @Column(name = "strikethrough")
    private Boolean strikethrough;

    @Enumerated(EnumType.STRING)
    @Column(name = "text_align")
    private TextAlign textAlign;

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 폰트, 크기, 손글씨 위치 등)
    public void update(String content) {
        this.content = content;
    }

}
