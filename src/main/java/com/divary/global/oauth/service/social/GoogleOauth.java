package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.avatar.service.AvatarService;
import com.divary.domain.device_session.service.DeviceSessionService;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
//import com.divary.global.redis.service.TokenBlackListService;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GoogleOauth implements SocialOauth {

    private final MemberService memberService;
    private final AvatarService avatarService;
    private final JwtTokenProvider jwtTokenProvider;
    private static final String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
    private final RestTemplate restTemplate;
    private final DeviceSessionService deviceSessionService;
    private final TokenBlackListService tokenBlackListService;


    private Map<String, Object> requestUserInfo(String accessToken) {


        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                userInfoUrl,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> userInfo = response.getBody();

            System.out.println("사용자 정보: " + userInfo);

            return userInfo;
        }

        throw new BusinessException(ErrorCode.GOOGLE_BAD_GATEWAY);
    }

    @Override
    @Transactional
    public LoginResponseDTO verifyAndLogin(String googleAccessToken, String deviceId) {
        // accessToken으로 사용자 정보 요청
        Map<String, Object> userInfo = requestUserInfo(googleAccessToken);
        String email = (String) userInfo.get("email");

        Member member;

        try {
            member = memberService.findMemberByEmail(email);
            deviceSessionService.removeRefreshToken(deviceId, member.getId());

        } catch (BusinessException e) {
            member = memberService.saveMember(Member.builder()
                    .email(email)
                    .role(Role.USER)
                    .build());

        }

        CustomUserPrincipal principal = new CustomUserPrincipal(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null,
                Collections.singleton(new SimpleGrantedAuthority(Role.USER.name()))  // 권한을 설정 일시적으로 일반 유저 권한만
        );

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        deviceSessionService.upsertRefreshToken(member, refreshToken, deviceId, SocialType.GOOGLE);



        // 3. 응답 생성
        return LoginResponseDTO.builder().accessToken(accessToken).refreshToken(refreshToken).build();

    }
    public void logout(String deviceId, Long userId, String accessToken) {
        //조건문 없이 바로 Access Token을 블랙리스트에 추가합니다.
        tokenBlackListService.addToBlacklist(accessToken);


        //DB에서 Refresh Token을 삭제합니다.
        deviceSessionService.removeRefreshToken(deviceId, userId);

        log.debug("로그아웃 처리 완료. AccessToken 블랙리스트 추가, RefreshToken 삭제. UserId: {}, DeviceId: {}", userId, deviceId);
    }
}