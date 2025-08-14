package com.divary.domain.logbase.logbook.dto.request;

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
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogDetailPutRequestDTO {

    @Schema(description = "저장 상태(임시저장:TEMP, 최종저장:COMPLETE)", example = "TEMP")
    private SaveStatus saveStatus;

    @Schema(description = "다이빙 장소", example = "제주도 서귀포시")
    private String place;

    @Schema(description = "다이빙 포인트 이름", example = "문섬")
    private String divePoint;

    @Schema(description = "다이빙 방식", example = "BOAT")
    private DiveMethod diveMethod;

    @Schema(description = "다이빙 목적", example = "FUN")
    private DivePurpose divePurpose;

    @Schema(description = "동행자 목록")
    private List<CompanionRequestDTO> companions;

    @Schema(description = "슈트 타입", example = "WETSUIT_3MM")
    private SuitType suitType;

    @Schema(description = "장비 정보", example = "장갑,후드,베스트")
    private String equipment;

    @Schema(description = "착용한 웨이트 무게(kg)", example = "6")
    private Integer weight;

    @Schema(description = "체감 무게", example = "HEAVY")
    private PerceiveWeight perceivedWeight;

    @Schema(description = "날씨 상태", example = "CLOUDY")
    private WeatherType weather;

    @Schema(description = "바람 세기", example = "WEAK")
    private Wind wind;

    @Schema(description = "조류 상태", example = "NONE")
    private Tide tide;

    @Schema(description = "파도 상태", example = "CALM")
    private Wave wave;

    @Schema(description = "기온", example = "28")
    private Integer temperature;

    @Schema(description = "수온", example = "24")
    private Integer waterTemperature;

    @Schema(description = "체감 온도", example = "COLD")
    private PerceiveTemp perceivedTemp;

    @Schema(description = "시야", example = "GOOD")
    private Sight sight;

    @Schema(description = "총 다이빙 시간(분 단위)", example = "45")
    private Integer diveTime;

    @Schema(description = "최대 수심 (단위:m)", example = "23")
    private Integer maxDepth;

    @Schema(description = "평균 수심 (단위:m)", example = "15")
    private Integer avgDepth;

    @Schema(description = "감압 정지 수심 (단위:m)", example = "6")
    private Integer decompressDepth;

    @Schema(description = "감압 정지 시간 (단위:분)", example = "3")
    private Integer decompressTime;

    @Schema(description = "다이빙 시작 시 탱크 압력 (단위:bar)", example = "200")
    private Integer startPressure;

    @Schema(description = "다이빙 종료 시 탱크 압력 (단위:bar)", example = "80")
    private Integer finishPressure;

    @Schema(description = "탱크 소비량 (단위:bar)", example = "120")
    private Integer consumption;
}
