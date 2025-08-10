package com.divary.domain.logbase.logbook.entity;


import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.logbook.enums.DiveMethod;
import com.divary.domain.logbase.logbook.enums.DivePurpose;
import com.divary.domain.logbase.logbook.enums.PerceiveTemp;
import com.divary.domain.logbase.logbook.enums.PerceiveWeight;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logbook.enums.Sight;
import com.divary.domain.logbase.logbook.enums.SuitType;
import com.divary.domain.logbase.logbook.enums.Tide;
import com.divary.domain.logbase.logbook.enums.Wave;
import com.divary.domain.logbase.logbook.enums.WeatherType;
import com.divary.domain.logbase.logbook.enums.Wind;
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
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Schema(description = "다이빙 로그 세부정보")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Setter
public class LogBook extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "log_base_info_id")
    @Schema(description = "로그북의 기본정보 외래키", example = "1")
    private LogBaseInfo logBaseInfo;

    @OneToMany(mappedBy = "logBook", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Schema(description = "동행자 리스트")
    @Builder.Default
    private List<Companion> companions = new ArrayList<>();

    @Column(name = "save_status")
    @Schema(description = "각 로그북의 저장 상태", example = "TEMP")
    private SaveStatus saveStatus;

    @Column(name = "accumulation",nullable = false)
    @Schema(description = "누적 횟수", example = "3")
    private int accumulation;

    @Column(name = "place",length = 50)
    @Schema(description = "다이빙 지역", example = "제주도 서귀포시")
    private String place;

    @Column(name = "dive_point",length = 50)
    @Schema(description = "다이빙 포인트", example = "문섬")
    private String divePoint;

    @Enumerated(EnumType.STRING)
    @Column(name = "dive_method")
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
