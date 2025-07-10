package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.embedded.Appearance;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Arrays;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "도감 생물 외모 응답 DTO")
public class AppearanceResponse {

    @Schema(description = "몸 형태", example = "말랑말랑하고 납작한 타원형")
    private String body;

    @Schema(description = "색상 코드", example = "[\"#FFFFFF\", \"#FFEB85\", \"#007BFF\"]")
    private List<String> colorCodes;

    @Schema(description = "색상 설명", example = "흰색, 노란색, 파란색 등 다양한 색")
    private String color;

    @Schema(description = "무늬", example = "몸통 위에 점, 줄무늬 등 화려한 패턴")
    private String pattern;

    @Schema(description = "기타 특징", example = "머리 부분에 촉수처럼 생긴 더듬이 두 개 있음")
    private String etc;

    public static AppearanceResponse from(Appearance appearance) {
        return AppearanceResponse.builder()
                .body(appearance.getBody())
                .colorCodes(parseColorCodes(appearance.getColorCodes()))
                .color(appearance.getColor())
                .pattern(appearance.getPattern())
                .etc(appearance.getEtc())
                .build();
    }

    private static List<String> parseColorCodes(String colorCodesStr) {
        if (colorCodesStr == null || colorCodesStr.isBlank()) {
            return List.of();
        }

        return Arrays.stream(colorCodesStr.split(","))
                .map(String::trim)
                .toList();
    }
}
