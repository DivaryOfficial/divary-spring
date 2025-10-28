package com.divary.global.oauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecoveryRequestDto {
    @NotBlank(message = "access 토큰은 필수 입력 값입니다.")
    private String accessToken;
}
