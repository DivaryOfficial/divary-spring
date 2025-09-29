package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.device_session.service.DeviceSessionService;
import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.infra.AppleJwtParser;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

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
    public LoginResponseDTO verifyAndLogin(String identityToken, String deviceId) {
        // Identity Token을 검증하고 사용자 정보를 추출합니다.
        Map<String, String> userInfo = appleJwtParser.parse(identityToken);
        String email = userInfo.get("email");

        Member member;
        try {
            member = memberService.findMemberByEmail(email);

        } catch (BusinessException e) {
            member = memberService.saveMember(Member.builder()
                    .email(email)
                    .role(Role.USER)
                    .build());

        }

        // 우리 서비스의 자체 토큰을 발급합니다.
        //    (이 부분은 기존 로그인 로직과 동일하게 구현합니다.)
        CustomUserPrincipal principal = new CustomUserPrincipal(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + Role.USER.name()))
        );
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // 디바이스 세션(리프레시 토큰) 저장
        deviceSessionService.saveToken(member, accessToken,refreshToken, deviceId, SocialType.APPLE);

        return new LoginResponseDTO(accessToken, refreshToken, SocialType.APPLE);
    }

    @Override
    public void logout(String deviceId, Long userId, String accessToken) {
        // AccessToken을 블랙리스트에 추가합니다.
        tokenBlackListService.addToBlacklist(accessToken);

        // DB에서 Refresh Token(디바이스 세션)을 삭제합니다.
        deviceSessionService.removeRefreshToken(deviceId, userId);

        log.debug("Apple 계정 로그아웃 처리 완료. UserId: {}, DeviceId: {}", userId, deviceId);
    }
}