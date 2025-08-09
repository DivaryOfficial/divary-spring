package com.divary.domain.avatar.dto;

import com.divary.domain.avatar.enums.BudyPet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuddyPetInfoDTO {

    @Schema(description = "버디펫 종류", example = "AXOLOTL")
    private BudyPet budyPet;

    @Schema(description = "회전 각도 (도 단위)", example = "15.0")
    private Double rotation;

    @Schema(description = "위치 오프셋 (width: X축, height: Y축)", example = "{ \"width\": 20.5, \"height\": -10.0 }")
    private Offset offset;

    @Data
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    public static class Offset {
        private Double width;
        private Double height;
    }
}
