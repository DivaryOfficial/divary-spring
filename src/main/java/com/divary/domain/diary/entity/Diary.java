package com.divary.domain.diary.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.entity.LogBaseInfo;
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
    public Diary(LogBaseInfo logBaseInfo, String contentJson) {
        this.logBaseInfo = logBaseInfo;
        this.contentJson = contentJson;
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "logBaseInfo_id", nullable = false, unique = true)
    private LogBaseInfo logBaseInfo;

    @Column(name = "content_json", columnDefinition = "LONGTEXT")
    @Schema(description = "일기 콘텐츠")
    private String contentJson;

    public void updateContent(String contentJson) {
        this.contentJson = contentJson;
    }
}

