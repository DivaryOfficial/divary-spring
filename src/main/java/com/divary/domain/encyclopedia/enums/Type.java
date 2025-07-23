package com.divary.domain.encyclopedia.enums;

import lombok.Getter;

@Getter
public enum Type {
    FISH("어류"),
    CRUSTACEAN("갑각류"),
    MOLLUSK("연체동물"),
    OTHER("기타");

    private final String description;

    Type(String description) {
        this.description = description;
    }
}