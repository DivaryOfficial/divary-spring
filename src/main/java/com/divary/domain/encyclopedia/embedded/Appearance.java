package com.divary.domain.encyclopedia.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "생물외모")
public class Appearance {

    @Schema(description = "몸 형태", example = "긴 타원형")
    private String body;

    @Schema(description = "색상 설명", example = "회색 바탕에 노란 점")
    private String color;

    @Schema(description = "무늬", example = "물결무늬")
    private String pattern;

    @Schema(description = "기타 설명", example = "등지느러미가 큼")
    private String etc;

    @Schema(description = "색상 코드", example = "[\"#FFFFFF\", \"#FFEB85\"]")
    private String colorCodes;

}
