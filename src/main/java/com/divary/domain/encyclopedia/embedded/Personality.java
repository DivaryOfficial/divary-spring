package com.divary.domain.encyclopedia.embedded;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(description = "생물성격")
public class Personality {

    @Schema(description = "활동성", example = "활발함")
    private String activity;

    @Schema(description = "사회성", example = "무리 생활을 선호함")
    private String socialSkill;

    @Schema(description = "행동 특성", example = "위협 시 모래에 숨음")
    private String behavior;

    @Schema(description = "반응성", example = "소리에 민감함")
    private String reactivity;

}
