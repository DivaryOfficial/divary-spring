package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.*;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import lombok.Getter;

@Getter
public class AvatarRequestDTO {
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
