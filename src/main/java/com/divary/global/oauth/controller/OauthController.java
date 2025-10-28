package com.divary.global.oauth.controller;


import com.divary.common.enums.SocialType;
import com.divary.common.response.ApiResponse;
import com.divary.domain.member.service.MemberService;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.exception.ErrorCode;
import com.divary.global.config.jwt.JwtResolver;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.oauth.dto.request.LogoutRequestDto;
import com.divary.global.oauth.dto.request.LoginRequestDto;
import com.divary.global.oauth.dto.response.DeactivateResponse;
import com.divary.global.oauth.dto.response.LoginResponseDTO;
import com.divary.global.oauth.service.OauthService;
import com.divary.global.redis.service.TokenBlackListService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
    private final MemberService memberService;
    private final TokenBlackListService tokenBlackListService;


    @PostMapping(value = "/{socialLoginType}/login")
    @Operation(summary = "로그인", description = "accessCode, device ID를 보내주세요")
    @ApiSuccessResponse(dataType = LoginResponseDTO.class)
    @ApiErrorExamples(value = {ErrorCode.INVALID_INPUT_VALUE})
    public ApiResponse<LoginResponseDTO> login(@PathVariable(name = "socialLoginType") SocialType socialLoginType,
                                               @Valid @RequestBody LoginRequestDto loginRequestDto) {
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

        oauthService.logout(socialLoginType, logoutRequestDto.getDeviceId(), userPrincipal.getId());
        tokenBlackListService.addToBlacklist(accessToken);
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

    @PostMapping(value = "/deactivate")
    @Operation(summary = "회원 탈퇴를 요청합니다.")
    @ApiSuccessResponse(dataType = DeactivateResponse.class)
    @ApiErrorExamples(value = {ErrorCode.MEMBER_NOT_FOUND})
    public ApiResponse<DeactivateResponse> deactivateUser(@AuthenticationPrincipal CustomUserPrincipal userPrincipal, HttpServletRequest request) {
        Long userId = userPrincipal.getId();
        String accessToken = jwtResolver.resolveAccessToken(request);

        DeactivateResponse response = memberService.requestToDeleteMember(userId);

        /**
         * Redis의 SADD 명령어는 Set에 멤버를 추가하는데, 이미 멤버가 존재하면 아무 작업도 하지 않고 성공을 반환합니다. 에러가 발생하지 않습니다.
         *이 경우, isContainToken을 호출하는 것은 불필요한 DB 조회(네트워크 왕복)를 한 번 더 하는 셈이므로 성능상 손해입니다.
         * 그냥 바로 addToBlacklist를 호출하는 것이 코드도 간결하고 효율적입니다.
         */
        tokenBlackListService.addToBlacklist(accessToken);

        return ApiResponse.success(response);
    }

    @PostMapping(value = "/{socialLoginType}/reactivate")
    @Operation(summary = "소셜 토큰을 사용하여 회원 탈퇴를 취소합니다.")
    @ApiSuccessResponse(dataType = void.class)
    @ApiErrorExamples(value = {ErrorCode.EMAIL_NOT_FOUND, ErrorCode.MEMBER_NOT_DEACTIVATED})
    public ApiResponse reactivate(@PathVariable(name = "socialLoginType") SocialType socialLoginType,
                                   @Valid @RequestBody com.divary.global.oauth.dto.request.RecoveryRequestDto recoveryRequestDto) {
        String accessToken = recoveryRequestDto.getAccessToken();
        log.info(">> 복구 요청 - 소셜 타입: {}, accessToken: {}", socialLoginType, accessToken);

        oauthService.reactivate(socialLoginType, accessToken);
        return ApiResponse.success("회원 정보 복구에 성공했습니다");
    }

}