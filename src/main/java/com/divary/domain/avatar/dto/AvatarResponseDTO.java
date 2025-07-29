package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvatarResponseDTO {
    private String name;
    private Tank tank;
    private BodyColor bodyColor;
    private BudyPet budyPet;
    private String bubbleText;
    private CheekColor cheekColor;
    private SpeechBubble speechBubble;
    private BuddyPetInfoDTO buddyPetInfo;
    private Mask mask;
    private Pin pin;
    private Regulator regulator;
    private Theme theme;
}
