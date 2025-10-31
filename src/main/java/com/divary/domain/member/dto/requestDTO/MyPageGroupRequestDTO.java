package com.divary.domain.member.dto.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class MyPageGroupRequestDTO {

    @Schema(description = "Group", example = "PADI")
    private String memberGroup;
}
