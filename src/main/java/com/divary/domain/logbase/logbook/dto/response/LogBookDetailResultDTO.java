package com.divary.domain.logbase.logbook.dto.response;

import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.logbook.entity.LogBook;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogBookDetailResultDTO {

    private Long LogBookId;
    private String name;
    private String icon;
    private LocalDate date;

    private String saveStatus;
    private Integer accumulation;

    private String place;
    private String divePoint;
    private String diveMethod;
    private String divePurpose;

    private List<CompanionResultDTO> companions;

    private String suitType;
    private String equipment;
    private Integer weight;
    private String perceivedWeight;

    private String weather;
    private String wind;
    private String tide;
    private String wave;
    private Integer temperature;
    private Integer waterTemperature;
    private String perceivedTemp;
    private String sight;

    private Integer diveTime;
    private Integer maxDepth;
    private Integer avgDepth;
    private Integer decompressDepth;
    private Integer decompressTime;
    private Integer startPressure;
    private Integer finishPressure;
    private Integer consumption;

    public static LogBookDetailResultDTO from(LogBook logBook, List<Companion> companions) {
        return LogBookDetailResultDTO.builder()
                .LogBookId(logBook.getId())
                .name(logBook.getLogBaseInfo().getName())
                .icon(logBook.getLogBaseInfo().getIconType().name())
                .saveStatus(Optional.ofNullable(logBook.getSaveStatus()).map(Enum::name).orElse(null))
                .accumulation(logBook.getAccumulation())
                .date(logBook.getLogBaseInfo().getDate())
                .place(logBook.getPlace())
                .divePoint(logBook.getDivePoint())
                .diveMethod(Optional.ofNullable(logBook.getDiveMethod()).map(Enum::name).orElse(null))
                .divePurpose(Optional.ofNullable(logBook.getDivePurpose()).map(Enum::name).orElse(null))
                .companions(Optional.ofNullable(companions).orElse(Collections.emptyList())
                        .stream()
                        .map(CompanionResultDTO::from)
                        .toList())
                .suitType(Optional.ofNullable(logBook.getSuitType()).map(Enum::name).orElse(null))
                .equipment(logBook.getEquipment())
                .weight(logBook.getWeight())
                .perceivedWeight(Optional.ofNullable(logBook.getPerceivedWeight()).map(Enum::name).orElse(null))
                .weather(Optional.ofNullable(logBook.getWeatherType()).map(Enum::name).orElse(null))
                .wind(Optional.ofNullable(logBook.getWind()).map(Enum::name).orElse(null))
                .tide(Optional.ofNullable(logBook.getTide()).map(Enum::name).orElse(null))
                .wave(Optional.ofNullable(logBook.getWave()).map(Enum::name).orElse(null))
                .temperature(logBook.getTemperature())
                .waterTemperature(logBook.getWaterTemperature())
                .perceivedTemp(Optional.ofNullable(logBook.getPerceivedTemp()).map(Enum::name).orElse(null))
                .sight(Optional.ofNullable(logBook.getSight()).map(Enum::name).orElse(null))
                .diveTime(logBook.getDiveTime())
                .maxDepth(logBook.getMaxDepth())
                .avgDepth(logBook.getAvgDepth())
                .decompressDepth(logBook.getDecompressDepth())
                .decompressTime(logBook.getDecompressTime())
                .startPressure(logBook.getStartPressure())
                .finishPressure(logBook.getFinishPressure())
                .consumption(logBook.getConsumption())
                .build();
    }

}
