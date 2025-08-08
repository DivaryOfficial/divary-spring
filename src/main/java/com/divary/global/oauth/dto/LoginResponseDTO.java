package com.divary.global.oauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponseDTO {
//    private Long id;
//    private String email;
//    private SocialType socialType;
//    private String name;
//    private Role role;
//    private Level level;
    private String token;
}
