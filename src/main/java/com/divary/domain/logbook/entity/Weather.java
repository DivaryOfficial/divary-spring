package com.divary.domain.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbook.enums.*;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "날씨 정보")
public class Weather extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "날씨 ID", example = "40")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "logBook_id")
    private LogBook logBook;

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
