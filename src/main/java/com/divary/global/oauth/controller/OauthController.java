package com.divary.global.oauth.controller;


import com.divary.common.enums.SocialType;
import com.divary.common.response.ApiResponse;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.exception.ErrorCode;
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
    @ApiSuccessResponse(dataType = LoginResponseDTO.class)
    @ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE})
    public ApiResponse<LoginResponseDTO> login(@PathVariable(name = "socialLoginType") SocialType socialLoginType,
                                     @RequestBody LoginRequestDto loginRequestDto) {
        String accessToken = loginRequestDto.getAccessToken();
        String deviceId = loginRequestDto.getDeviceId();
        log.info(">> 앱에서 받은 accessToken :: {}", accessToken);

        LoginResponseDTO responseDto = oauthService.authenticateWithAccessToken(socialLoginType, accessToken, deviceId);
        return ApiResponse.success(responseDto);
    }
}