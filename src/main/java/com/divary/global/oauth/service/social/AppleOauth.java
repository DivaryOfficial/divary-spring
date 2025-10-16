package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.enums.Status;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.device_session.service.DeviceSessionService;
import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.infra.AppleJwtParser;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppleOauth implements SocialOauth {

    private final DeviceSessionService deviceSessionService;
    private final TokenBlackListService tokenBlackListService;
    private final MemberService memberService; // 회원 조회/가입을 위한 의존성 추가
    private final JwtTokenProvider jwtTokenProvider; // 우리 서비스의 토큰 발급을 위한 의존성 추가
    private final AppleJwtParser appleJwtParser; // Apple Identity Token을 검증/해독하는 별도 클래스


    /**
     * 클라이언트로부터 받은 Identity Token을 검증하고, 우리 서비스의 로그인 처리를 수행합니다.
     * @param identityToken 클라이언트에서 전달받은 Apple Identity Token
     * @param deviceId 사용자의 기기 ID
     * @return 우리 서비스의 Access/Refresh Token이 담긴 DTO
     */
    @Override
    @Transactional
    public LoginResponseDTO verifyAndLogin(String identityToken, String deviceId) {
        // Identity Token을 검증하고 사용자 정보를 추출합니다.
        Map<String, String> userInfo = appleJwtParser.parse(identityToken);
        String email = userInfo.get("email");

        Member member = memberService.findOrCreateMember(email);

        CustomUserPrincipal principal = new CustomUserPrincipal(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null,
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.name()))  // 권한을 설정 일시적으로 일반 유저 권한만
        );

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        deviceSessionService.upsertRefreshToken(member, refreshToken, deviceId, SocialType.APPLE);



        // 3. 응답 생성
        return LoginResponseDTO.builder().accessToken(accessToken).refreshToken(refreshToken).build();

    }

    @Override
    public void logout(String deviceId, Long userId) {

        // DB에서 Refresh Token(디바이스 세션)을 삭제합니다.
        deviceSessionService.removeRefreshToken(deviceId, userId);

        log.debug("Apple 계정 로그아웃 처리 완료. UserId: {}, DeviceId: {}", userId, deviceId);
    }
    @Override
    public SocialType getType() {
        return SocialType.APPLE;
    }
}