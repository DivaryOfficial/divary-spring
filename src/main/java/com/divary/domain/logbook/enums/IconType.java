package com.divary.domain.logbook.enums;

public enum IconType {
    CLOWNFISH("흰동가리"),
    BUTTERFLYFISH("나비고기"),
    OCTOPUS("문어"),
    CLEANER_WRASSE("청줄놀래기"),
    BLACK_ROCKFISH("쏨배기"),
    SEA_HARE("군소"),
    PUFFERFISH("복어"),
    STRIPED_BEAKFISH("돌돔"),
    NUDIBRANCH("갯민숭달팽이"),
    MOON_JELLYFISH("보름달물해파리"),
    YELLOWTAIL_SCAD("줄전갱이"),
    MANTIS_SHRIMP("끄덕새우"),
    SEA_TURTLE("바다거북"),
    STARFISH("불가사리"),
    RED_LIONFISH("쏠배감펭"),
    SEA_URCHIN("성게");

    private final String description;

    IconType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
