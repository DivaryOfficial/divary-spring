package com.divary.global.oauth.service;

import com.divary.common.enums.SocialType;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.service.social.SocialOauth;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OauthService {
    private final List<SocialOauth> socialOauthList;
    private final SocialOauth socialOauth;


    private SocialOauth findSocialOauthByType(SocialType socialLoginType) {
        return socialOauthList.stream()
                .filter(x -> x.type() == socialLoginType)
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    public LoginResponseDTO authenticateWithAccessToken(SocialType socialLoginType, String accessToken, String deviceId) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        if (socialOauth == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        return socialOauth.verifyAndLogin(accessToken, deviceId);
    }

    @Transactional
    public void logout(SocialType socialLoginType, String deviceId, Long userId) {
        SocialOauth socialOauth = this.findSocialOauthByType(socialLoginType);
        if (socialOauth == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        socialOauth.logout(deviceId, userId);
    }
}
