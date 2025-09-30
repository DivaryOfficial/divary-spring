package com.divary.global.oauth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank(message = "access 토큰은 필수 입력 값입니다.")
    private String accessToken;
    @NotBlank(message = "디바이스 아이디는 필수 입력 값입니다.")
    private String deviceId;
}
