package com.divary.global.oauth.controller;


import com.divary.common.enums.SocialType;
import com.divary.common.response.ApiResponse;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.exception.ErrorCode;
import com.divary.global.config.jwt.JwtResolver;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.oauth.dto.request.LogoutRequestDto;
import com.divary.global.oauth.dto.request.LoginRequestDto;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.service.OauthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(value = "/auth")
@Slf4j
public class OauthController {
    private final OauthService oauthService;
    private final JwtResolver jwtResolver;


    @PostMapping(value = "/{socialLoginType}/login")
    @Operation(summary = "로그인", description = "accessCode, device ID를 보내주세요")
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

    @DeleteMapping(value = "/{socialLoginType}/logout")
    @Operation(summary = "로그아웃")
    @ApiSuccessResponse(dataType = LoginResponseDTO.class)
    @ApiErrorExamples(value = {ErrorCode.DEVICE_ID_NOT_FOUND})
    public ApiResponse logout(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, @PathVariable(name = "socialLoginType") SocialType socialLoginType, HttpServletRequest request, @RequestBody LogoutRequestDto logoutRequestDto) {
        String accessToken = jwtResolver.resolveAccessToken(request);

        oauthService.logout(socialLoginType, logoutRequestDto.getDeviceId(), userPrincipal.getId(), accessToken);
        return ApiResponse.success("로그아웃에 성공했습니다.");
    }

    @PostMapping(value = "/reissue")
    @Operation(summary = "access token 만료시 새로운 access token 발급")
    @ApiSuccessResponse(dataType = LoginResponseDTO.class)
    @ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE})
    public ApiResponse<LoginResponseDTO> reissueToken(HttpServletRequest request) {
        String refreshToken = jwtResolver.resolveRefreshToken(request); // 헤더에서 Refresh Token 추출
        String deviceId = request.getHeader("Device-Id");

        LoginResponseDTO newTokens = oauthService.reissueToken(refreshToken, deviceId);

        return ApiResponse.success(newTokens);
    }
}