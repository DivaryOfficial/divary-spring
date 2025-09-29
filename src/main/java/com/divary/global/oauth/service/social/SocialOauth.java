package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.global.oauth.dto.response.LoginResponseDTO;

public interface SocialOauth {
    LoginResponseDTO verifyAndLogin(String token, String deviceId);
    void logout(String deviceId, Long userId, String accessToken);
    SocialType getType();

}
