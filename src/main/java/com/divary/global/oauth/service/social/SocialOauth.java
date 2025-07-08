package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;

public interface SocialOauth {
    String verifyAndLogin(String code);

    default SocialType type() {
        if (this instanceof GoogleOauth) { //다른 소셜 로그인 추가 가능
            return SocialType.GOOGLE;
        } else {
            System.out.println("NuLL soical Type");
            return null;
        }
    }

}
