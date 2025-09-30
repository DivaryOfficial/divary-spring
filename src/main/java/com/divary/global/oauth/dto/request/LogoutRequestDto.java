package com.divary.global.oauth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class LogoutRequestDto {
    @Schema(description = "디바이스 아이디", example = "1")
    private String deviceId;
}
