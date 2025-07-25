package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.global.oauth.dto.LoginResponseDTO;

public interface SocialOauth {
    LoginResponseDTO verifyAndLogin(String code);
    SocialType type = null;

    default SocialType type() {
        if (this instanceof GoogleOauth) { //다른 소셜 로그인 추가 가능
            return SocialType.GOOGLE;
        } else {
            System.out.println("NuLL soical Type");
            return null;
        }
    }

}
