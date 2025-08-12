package com.divary.global.oauth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDTO {

    private String accessToken;
    private String refreshToken;
}
