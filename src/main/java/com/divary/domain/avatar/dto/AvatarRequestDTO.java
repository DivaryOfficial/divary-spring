package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class AvatarRequestDTO {
    @Schema(description = "아바타 이름", example = "아진봇", nullable = true)
    @Size(max = 20, message = "이름은 최대 20자까지 입력 가능합니다.")
    @Pattern(
            regexp = "^(?!\\s*$)[\\p{L}\\p{N}\\p{Zs}\\p{So}]{1,20}$",
            message = "이름은 특수문자를 제외한 한글, 영문, 숫자, 공백, 이모지만 사용할 수 있습니다."
    )
    private String name;

    @Schema(description = "탱크 색깔", example = "WHITE", nullable = true)
    private Tank tank;

    @Schema(description = "몸 색상", example = "IVORY", nullable = true)
    private BodyColor bodyColor;

    @Schema(description = "버디펫", example = "AXOLOTL", nullable = true)
    private BudyPet budyPet;

    @Schema(description = "볼 색상", example = "PINK", nullable = true)
    private CheekColor cheekColor;

    @Schema(description = "말풍선 타입", example = "WHITE", nullable = true) //임시 enum 값 바꿔야 함
    private SpeechBubble speechBubble;

    @Schema(description = "마스크 샐깔", example = "WHITE", nullable = true)
    private Mask mask;

    @Schema(description = "핀 색깔", example = "WHITE", nullable = true)
    private Pin pin;

    @Schema(description = "레귤레이터 색깔", example = "BLACK", nullable = true)
    private Regulator regulator;

    @Schema(description = "테마", example = "CORAL_FOREST")
    private Theme theme;
}
