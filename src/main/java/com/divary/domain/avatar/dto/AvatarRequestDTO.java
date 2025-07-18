package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AvatarRequestDTO {
    //수정할 필드만 받을거라 validation이 필요없어 보입니다
    private String name;
    private Tank tank;
    private BodyColor bodyColor;
    private BudyPet budyPet;
    private CheekColor cheekColor;
    private SpeechBubble speechBubble;
    private Mask mask;
    private Pin pin;
    private Regulator regulator;
    private Theme theme;
}
