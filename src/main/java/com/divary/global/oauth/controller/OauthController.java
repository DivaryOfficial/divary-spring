package com.divary.global.oauth.controller;


import com.divary.common.enums.SocialType;
import com.divary.global.oauth.dto.LoginRequestDto;
import com.divary.global.oauth.service.OauthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OauthController {
    private final OauthService oauthService;

//    @GetMapping(value = "/{socialLoginType}")
//    public void socialLoginType(@PathVariable(name = "socialLoginType") SocialType socialLoginType) {
//        log.info(">> 사용자로부터 SNS 로그인 요청을 받음 :: {} Social Login", socialLoginType);
//        oauthService.request(socialLoginType);
//    }

    @PostMapping(value = "/{socialLoginType}/login")
    public String callback(@PathVariable(name = "socialLoginType") SocialType socialLoginType,
                           @RequestBody LoginRequestDto loginRequestDto) {
        String accessToken = loginRequestDto.getAccessToken();
        log.info(">> 앱에서 받은 accessToken :: {}", accessToken);

        return oauthService.authenticateWithAccessToken(socialLoginType, accessToken);
    }
}