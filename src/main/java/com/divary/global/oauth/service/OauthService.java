package com.divary.global.oauth.service;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.device_session.entity.DeviceSession;
import com.divary.domain.device_session.repository.DeviceSessionRepository;
import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.service.social.SocialOauth;
import com.divary.global.oauth.service.social.SocialOauthServiceFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OauthService {
    private final List<SocialOauth> socialOauthList;
    private final JwtTokenProvider jwtTokenProvider;
    private final DeviceSessionRepository deviceSessionRepository;
    private final SocialOauthServiceFactory socialOauthServiceFactory;


    public SocialOauth findSocialOauthByType(SocialType socialType) {
        return socialOauthServiceFactory.getInstance(socialType);
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

    /**
     * 우리 서비스의 Refresh Token을 사용하여 Access Token과 Refresh Token을 재발급합니다. (RTR)
     * 이 로직은 모든 소셜 로그인 사용자에게 공통으로 적용됩니다.
     * @param refreshToken 클라이언트로부터 받은 Refresh Token
     * @param deviceId 사용자의 기기 ID
     * @return 새로운 Access/Refresh Token이 담긴 DTO
     */
    @Transactional
    public LoginResponseDTO reissueToken(String refreshToken, String deviceId) {
        // 1. Refresh Token 유효성 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Refresh Token이 유효하지 않습니다.");
        }

        // 2. DB에 저장된 토큰과 일치하는지, Device ID가 맞는지 확인
        boolean exists = deviceSessionRepository.existsByRefreshTokenAndDeviceId(refreshToken, deviceId);
        if (!exists) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND, "저장소에 Refresh Token이 없거나 기기 정보가 일치하지 않습니다.");
        }

        // 3. 토큰에서 사용자 ID 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        Role role = jwtTokenProvider.getRoleFromToken(refreshToken);




        // 4. 새로운 Access Token과 Refresh Token 생성 (RTR)
        CustomUserPrincipal principal = new CustomUserPrincipal(userId, role);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);


        // 5. DB의 Refresh Token을 새로운 토큰으로 업데이트
        DeviceSession deviceSession = deviceSessionRepository.findByUser_IdAndDeviceId(userId, deviceId);
        deviceSession.updateToken(newRefreshToken);

        // 6. 새로운 토큰 쌍을 반환
        return new LoginResponseDTO(newAccessToken, newRefreshToken);
    }
}

