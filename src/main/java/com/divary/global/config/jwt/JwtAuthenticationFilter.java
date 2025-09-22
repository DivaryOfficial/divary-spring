package com.divary.global.config.jwt;

import com.divary.common.response.ApiResponse;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.redis.service.TokenBlackListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * 클라이언트의 모든 API 요청을 가로채 Access Token의 유효성을 검증하는 필터입니다.
 * 토큰 재발급 로직은 처리하지 않으며, 토큰이 유효하지 않을 경우 예외를 발생시켜 401 Unauthorized 응답을 유도합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtResolver jwtResolver;
    private final TokenBlackListService tokenBlackListService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {


        try {
            // 2. 헤더에서 Access Token을 추출합니다.
            String accessToken = jwtResolver.resolveAccessToken(request);

            // 3. Access Token이 존재하는 경우에만 검증을 시작합니다.
            if (StringUtils.hasText(accessToken)) {

                //토큰이 유효한지 검증합니다.
                if (jwtTokenProvider.validateToken(accessToken)) {
                    // 토큰이 유효하면, 로그아웃 처리된 토큰인지 블랙리스트를 확인하고 인증 정보를 SecurityContext에 등록합니다.

                    if (tokenBlackListService.isContainToken(accessToken)) {
                        throw new BusinessException(ErrorCode.INVALID_TOKEN, "로그아웃 처리된 토큰입니다.");
                    }

                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("SecurityContext에 인증 정보 설정 완료 - 사용자 ID: {}", authentication.getName());
                } else {
                    // 토큰이 만료되었거나 유효하지 않은 경우, 예외를 발생시킵니다.
                    log.warn("유효하지 않은 Access Token입니다. URI: {}", request.getRequestURI());
                    throw new BusinessException(ErrorCode.ACCESS_TOKEN_EXPIRED);
                }
            }
            // 4. Access Token이 헤더에 없는 경우, 일단 통과시킵니다.
            // (이후 SecurityConfig에서 .anyRequest().authenticated() 설정에 따라 접근이 차단됩니다.)

        } catch (BusinessException e) {
            // JWT 관련 예외는 정형화된 JSON 응답으로 처리합니다.
            handleJwtException(response, e.getErrorCode(), request.getRequestURI());
            return; // 예외 발생 시 필터 체인 중단
        }

        filterChain.doFilter(request, response);
    }


    /**
     * JWT 관련 예외 발생 시, 정형화된 에러 응답을 생성합니다.
     */
    private void handleJwtException(HttpServletResponse response, ErrorCode errorCode, String path) {
        try {
            response.setStatus(errorCode.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            ApiResponse<Void> errorResponse = ApiResponse.error(errorCode, path);
            String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
        } catch (IOException e) {
            log.error("JWT 예외 응답 작성 중 오류 발생: {}", e.getMessage());
        }
    }
}