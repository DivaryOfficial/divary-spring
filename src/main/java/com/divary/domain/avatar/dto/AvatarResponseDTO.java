package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AvatarResponseDTO {
    private String name;
    private Accessories accessory;
    private BodyColor bodyColor;
    private BudyPet budyPet;
    private CheekColor cheekColor;
    private EyeColor eyeColor;
    private Eyelash eyelash;
    private Mask mask;
    private Pin pin;
    private Regulator regulator;
    private Theme theme;
}
