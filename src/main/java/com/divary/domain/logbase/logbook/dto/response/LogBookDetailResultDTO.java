package com.divary.domain.logbase.logbook.dto.response;

import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.logbook.entity.LogBook;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogBookDetailResultDTO {

    private Long LogBookId;
    private String name;
    private String icon;
    private LocalDate date;

    private SaveStatus saveStatus;
    private int accumulation;

    private String place;
    private String divePoint;
    private String diveMethod;
    private String divePurpose;

    private List<CompanionResultDTO> companions;

    private String suitType;
    private String equipment;
    private int weight;
    private String perceivedWeight;

    private String weather;
    private String wind;
    private String tide;
    private String wave;
    private int temperature;
    private int waterTemperature;
    private String perceivedTemp;
    private String sight;

    private int diveTime;
    private int maxDepth;
    private int avgDepth;
    private int decompressDepth;
    private int decompressTime;
    private int startPressure;
    private int finishPressure;
    private int consumption;

    public static LogBookDetailResultDTO from(LogBook logBook, List<Companion> companions) {
        return LogBookDetailResultDTO.builder()
                .LogBookId(logBook.getId())
                .name(logBook.getLogBaseInfo().getName())
                .icon(logBook.getLogBaseInfo().getIconType().name())
                .saveStatus(logBook.getSaveStatus())
                .accumulation(logBook.getAccumulation())
                .date(logBook.getLogBaseInfo().getDate())
                .place(logBook.getPlace())
                .divePoint(logBook.getDivePoint())
                .diveMethod(logBook.getDiveMethod().name())
                .divePurpose(logBook.getDivePurpose().name())
                .companions(companions.stream()
                        .map(CompanionResultDTO::from)
                        .toList())
                .suitType(logBook.getSuitType().name())
                .equipment(logBook.getEquipment())
                .weight(logBook.getWeight())
                .perceivedWeight(logBook.getPerceivedWeight().name())
                .weather(logBook.getWeatherType().name())
                .wind(logBook.getWind().name())
                .tide(logBook.getTide().name())
                .wave(logBook.getWave().name())
                .temperature(logBook.getTemperature())
                .waterTemperature(logBook.getWaterTemperature())
                .perceivedTemp(logBook.getPerceivedTemp().name())
                .sight(logBook.getSight().name())
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
