package com.divary.domain.mypage.dto.requestDTO;

import com.divary.domain.member.enums.Levels;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MyPageLevelRequestDTO {
    @Schema(description = "Levels", example = "OPEN_WATER_DIVER", nullable = false)
    private Levels level;
}
