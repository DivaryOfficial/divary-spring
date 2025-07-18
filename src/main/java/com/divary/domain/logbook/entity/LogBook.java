package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.logbook.enums.*;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Getter
@Schema(description = "다이빙 로그")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class LogBook extends BaseEntity {

   @OneToOne(mappedBy = "logBook", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "name", nullable = false, length = 40)
    @Schema(description = "로그 제목", example = "고래 원정")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "icon_type", nullable = false)
    @Schema(description = "아이콘 타입", example = "WHALE")
    private IconType iconType;

    @Column(name = "accumulation",nullable = false)
    @Schema(description = "누적 횟수", example = "3")
    private int accumulation;

    @Column(name = "save_status",nullable = false)
    private SaveStatus saveStatus = SaveStatus.TEMP;

    @Column(name = "date", nullable = false)
    @Schema(description = "다이빙 날짜", example = "2025-07-25")
    private LocalDate date;

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

    @Column(name = "dive_time")
    @Schema(description = "총 다이빙 시간(분)", example = "45")
    private Integer diveTime;

    @Column(name = "max_depth")
    @Schema(description = "최대 수심(m)", example = "23")
    private Integer maxDepth;

    @Column(name = "avg_depth")
    @Schema(description = "평균 수심(m)", example = "15")
    private Integer avgDepth;

    @Column(name = "decompress_depth")
    @Schema(description = "감압정지 깊이(m)", example = "6")
    private Integer decompressDepth;

    @Column(name = "decompress_time")
    @Schema(description = "감압정지 시간(분)", example = "3")
    private Integer decompressTime;

    @Column(name = "start_pressure")
    @Schema(description = "시작 탱크 압력(bar)", example = "200")
    private Integer startPressure;

    @Column(name = "finish_pressure")
    @Schema(description = "종료 탱크 압력(bar)", example = "80")
    private Integer finishPressure;

    @Column(name = "consumption")
    @Schema(description = "기체 소비량(bar)", example = "120")
    private Integer consumption;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_type")
    @Schema(description = "날씨 상태", example = "PARTLY_CLOUDY")
    private WeatherType weatherType;

    @Enumerated(EnumType.STRING)
    @Column(name = "wind")
    @Schema(description = "바람 세기", example = "약풍")
    private Wind wind;

    @Enumerated(EnumType.STRING)
    @Column(name = "tide")
    @Schema(description = "조류 세기", example = "미류")
    private Tide tide;

    @Enumerated(EnumType.STRING)
    @Column(name = "wave")
    @Schema(description = "파도 세기", example = "중간")
    private Wave wave;

    @Column(name = "temperature")
    @Schema(description = "기온 (℃)", example = "24")
    private Integer temperature;

    @Column(name = "water_temperature")
    @Schema(description = "수온 (℃)", example = "21")
    private Integer waterTemperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "perceived_temp")
    @Schema(description = "체감 온도", example = "추움")
    private PerceiveTemp perceivedTemp;

    @Enumerated(EnumType.STRING)
    @Column(name = "sight")
    @Schema(description = "시야 거리", example = "좋음")
    private Sight sight;

}
