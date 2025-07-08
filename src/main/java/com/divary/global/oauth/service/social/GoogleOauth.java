package com.divary.global.oauth.service.social;

import com.divary.common.enums.SocialType;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.enums.Role;
import com.divary.domain.Member.service.MemberService;
import com.divary.global.oauth.dto.LoginResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GoogleOauth implements SocialOauth {

    private final MemberService memberService;

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

        throw new RuntimeException("사용자 정보 조회 실패");
    }

    @Override
    public LoginResponseDTO verifyAndLogin(String accessToken) {
        // accessToken으로 사용자 정보 요청
        Map<String, Object> userInfo = requestUserInfo(accessToken);
        String email = (String) userInfo.get("email");
        String name = (String)userInfo.get("name");

        Member member;

        try {
            member = memberService.findMemberByEmail(email);
        } catch (IllegalArgumentException e) {
            member = memberService.saveMember(Member.builder()
                    .email(email)
                    .socialType(SocialType.GOOGLE)
                    .role(Role.User)
                    .build());
        }

        // 3. 응답 생성
        return LoginResponseDTO.builder()
                .email(member.getEmail())
                .id(member.getId())
                .name(name)
                .socialType(member.getSocialType())
                .role(member.getRole())
                .build();

    }
}