package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.service.social.SocialOauth;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class SocialOauthServiceFactory {

    private final Map<SocialType, SocialOauth> services = new EnumMap<>(SocialType.class);

    // 생성자에서 Spring이 SocialOauth를 구현하는 모든 Bean을 List로 주입해줍니다.
    public SocialOauthServiceFactory(List<SocialOauth> socialOauthServices) {
        for (SocialOauth service : socialOauthServices) {
            services.put(service.getType(), service);
        }
    }

    // 소셜 타입에 맞는 서비스(Bean)를 반환합니다.
    public SocialOauth getInstance(SocialType socialType) {
        return Optional.ofNullable(services.get(socialType))
                .orElseThrow(() -> new BusinessException(ErrorCode.SOCIAL_PROVIDER_NOT_FOUND));
    }
}