package com.divary.global.config.jwt;

import com.divary.common.response.ApiResponse;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.token.service.RefreshTokenService;
import com.divary.global.config.security.CustomUserDetailsService;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
//import com.divary.global.redis.service.TokenBlackListService;
import com.divary.global.redis.service.TokenBlackListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final JwtResolver jwtResolver;
    private final TokenBlackListService tokenBlackListService;


    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("JWT 필터 처리 시작 - URI: {}", requestURI);
        
        try {
            String accessToken = jwtResolver.resolveAccessToken(request);
            String refreshToken = jwtResolver.resolveRefreshToken(request);
            String deviceId = request.getHeader("Device-Id");
            log.debug("AccessToken: {}", accessToken != null ? "존재" : "없음");
            log.debug("RefreshToken: {}", refreshToken != null ? "존재" : "없음");
            log.debug("DeviceId: {}", deviceId != null ? "존재" : "없음");

            if (accessToken != null && tokenBlackListService.isContainToken(accessToken)) { //토큰 블랙리스트
                throw new Exception("<< 경고 >>만료된 토큰으로 접근하려합니다!!!");
            }

            if(StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("SecurityContext에 인증 정보 설정 완료 - 사용자: {}", authentication.getName());


            }
            // accessToken이 만료 && refreshToken 존재 시 재발급
            else if (!jwtTokenProvider.validateToken(accessToken) && StringUtils.hasText(refreshToken)) {
                boolean validateRefreshToken = jwtTokenProvider.validateToken(refreshToken);
                boolean existsRefreshToken = jwtTokenProvider.existsRefreshToken(refreshToken, deviceId);

                if (validateRefreshToken && existsRefreshToken) {
                    Member user = jwtTokenProvider.getUserFromToken(refreshToken);



                    CustomUserPrincipal principal = new CustomUserPrincipal(user);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
                    String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication); // access토큰 발급할때마다 refresh도 새로 발급(RTR)

                    refreshTokenService.updateRefreshToken(user.getId(), deviceId, newRefreshToken);
                    // 새 토큰 헤더에 추가
                    jwtTokenProvider.setHeaderTokens(response, newAccessToken, newRefreshToken);


                    // 인증 객체 설정
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("AccessToken 재발급 및 SecurityContext 설정 완료 - 사용자: {}", authentication.getName());
                } else {
                    if (!validateRefreshToken && existsRefreshToken) {
                        // DB에 있으나 만료된 리프레시 토큰은 삭제
                        jwtTokenProvider.deleteRefreshToken(refreshToken);
                        log.info("만료된 RefreshToken 삭제 완료");
                    }
                    log.warn("RefreshToken이 유효하지 않거나 저장소에 없음");
                    throw new BusinessException(ErrorCode.INVALID_TOKEN);
                }
            } else {
                log.debug("토큰이 없거나 둘 다 유효하지 않음");
            }


        } catch (BusinessException e) {
            log.error("JWT 인증 비즈니스 로직 오류: {}", e.getMessage());
            handleJwtException(request, response, e.getErrorCode());
            return;
        } catch (Exception e) {
            log.error("JWT 인증 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            handleJwtException(request, response, ErrorCode.INVALID_TOKEN);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    // 인증 예외 처리 정형화된 구조로 응답하도록 설정 (GlobalExceptionHandler로 처리 불가 해서 직접 처리)
    private void handleJwtException(HttpServletRequest request, HttpServletResponse response, ErrorCode errorCode) {
        try {
            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            
            ApiResponse<Void> errorResponse = ApiResponse.error(errorCode, request.getRequestURI());
            String jsonResponse = objectMapper.writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            
        } catch (IOException e) {
            log.error("JWT 예외 응답 작성 중 오류 발생: {}", e.getMessage());
        }
    }

}