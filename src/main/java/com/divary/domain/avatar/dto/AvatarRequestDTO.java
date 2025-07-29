package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class AvatarRequestDTO {
    @Schema(description = "아바타 이름", example = "아진봇")
    @Size(max = 20, message = "이름은 최대 20자까지 입력 가능합니다.")
    @Pattern(
            regexp = "^(?!\\s*$)[\\p{L}\\p{N}\\p{Zs}\\p{So}]{1,20}$",
            message = "이름은 특수문자를 제외한 한글, 영문, 숫자, 공백, 이모지만 사용할 수 있습니다."
    )
    private String name;

    @Schema(description = "탱크 색깔", example = "YELLOW")
    private Tank tank;

    @Schema(description = "몸 색상", example = "IVORY")
    private BodyColor bodyColor;

    @Schema(description = "버디펫 정보 리스트")
    private BuddyPetInfoDTO buddyPetInfo;


    @Schema(description = "말풍선 텍스트", example = "Hi i'm buddy")
    private String bubbleText;

    @Schema(description = "볼 색상", example = "PINK")
    private CheekColor cheekColor;

    @Schema(description = "말풍선 타입", example = "NONE")
    private SpeechBubble speechBubble;

    @Schema(description = "마스크 샐깔", example = "WHITE")
    private Mask mask;

    @Schema(description = "핀 색깔", example = "WHITE")
    private Pin pin;

    @Schema(description = "레귤레이터 색깔", example = "BLACK")
    private Regulator regulator;

    @Schema(description = "테마", example = "CORAL_FOREST")
    private Theme theme;
}
