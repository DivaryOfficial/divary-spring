package com.divary.global.oauth.controller;


import com.divary.common.enums.SocialType;
import com.divary.common.response.ApiResponse;
import com.divary.global.oauth.dto.LoginRequestDto;
import com.divary.global.oauth.dto.LoginResponseDTO;
import com.divary.global.oauth.service.OauthService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OauthController {
    private final OauthService oauthService;


    @PostMapping(value = "/{socialLoginType}/login")
    @Operation(summary = "로그인", description = "accessCode를 보내주세요")
    public ApiResponse<LoginResponseDTO> login(@PathVariable(name = "socialLoginType") SocialType socialLoginType,
                                     @RequestBody LoginRequestDto loginRequestDto) {
        String accessToken = loginRequestDto.getAccessToken();
        log.info(">> 앱에서 받은 accessToken :: {}", accessToken);

        LoginResponseDTO responseDto = oauthService.authenticateWithAccessToken(socialLoginType, accessToken);
        return ApiResponse.success(responseDto);
    }
}