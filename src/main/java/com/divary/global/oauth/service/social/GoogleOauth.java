package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.enums.Role;
import com.divary.domain.Member.service.MemberService;
import com.divary.global.config.security.jwt.JwtTokenProvider;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;


    private Map<String, Object> requestUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";

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

        throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
    }

    @Override
    public LoginResponseDTO verifyAndLogin(String googleAccessToken) {
        // accessToken으로 사용자 정보 요청
        Map<String, Object> userInfo = requestUserInfo(googleAccessToken);
        String email = (String) userInfo.get("email");
        String name = (String)userInfo.get("name");

        Member member;

        try {
            member = memberService.findMemberByEmail(email);
        } catch (IllegalArgumentException e) {
            member = memberService.saveMember(Member.builder()
                    .email(email)
                    .socialType(SocialType.GOOGLE)
                    .role(Role.USER)
                    .build());
        }


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                email, null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + Role.USER.name()))  // 권한을 설정 일시적으로 일반 유저 권한만
        );

        // JWT 토큰 발급
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 3. 응답 생성
        return LoginResponseDTO.builder().token(accessToken).build();

    }
}