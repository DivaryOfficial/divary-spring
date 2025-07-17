package com.divary.global.oauth.service;

import com.divary.common.enums.SocialType;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.LoginResponseDTO;
import com.divary.global.oauth.service.social.SocialOauth;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OauthService {
    private final List<SocialOauth> socialOauthList;


    private SocialOauth findSocialOauthByType(SocialType socialLoginType) {
        return socialOauthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    public LoginResponseDTO authenticateWithAccessToken(SocialType socialLoginType, String accessToken) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        if (socialOauth == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return socialOauth.verifyAndLogin(accessToken);
    }
}