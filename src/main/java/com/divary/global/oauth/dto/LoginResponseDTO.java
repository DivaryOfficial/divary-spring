package com.divary.global.oauth.dto;

import com.divary.common.enums.SocialType;
import com.divary.domain.Member.enums.Level;
import com.divary.domain.Member.enums.Role;
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
