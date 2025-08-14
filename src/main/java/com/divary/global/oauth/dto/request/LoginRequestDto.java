package com.divary.global.oauth.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    private String accessToken;
    private String deviceId;
    private Boolean rememberLogin;
}
