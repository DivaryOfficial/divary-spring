package com.divary.global.config.security.jwt;

import com.divary.common.response.ApiResponse;
import com.divary.domain.member.enums.Role;
import com.divary.global.config.properties.Constants;
import com.divary.global.config.security.CustomUserDetailsService;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("JWT 필터 처리 시작 - URI: {}", requestURI);
        
        try {
            String accessToken = resolveAccessToken(request);
            String refreshToken = resolveRefreshToken(request);
            log.debug("AccessToken: {}", accessToken != null ? "존재" : "없음");
            log.debug("RefreshToken: {}", refreshToken != null ? "존재" : "없음");

            if(StringUtils.hasText(accessToken) && jwtTokenProvider.validateToken(accessToken)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("SecurityContext에 인증 정보 설정 완료 - 사용자: {}", authentication.getName());
            }
            // accessToken이 만료 && refreshToken 존재 시 재발급
            else if (!jwtTokenProvider.validateToken(accessToken) && StringUtils.hasText(refreshToken)) {
                boolean validateRefreshToken = jwtTokenProvider.validateToken(refreshToken);
                boolean existsRefreshToken = jwtTokenProvider.existsRefreshToken(refreshToken);

                if (validateRefreshToken && existsRefreshToken) {
                    String email = jwtTokenProvider.getUserEmail(refreshToken);
                    List<String> roles = jwtTokenProvider.getRoles(email);

                    List<GrantedAuthority> authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    CustomUserPrincipal principal = (CustomUserPrincipal) customUserDetailsService.loadUserByUsername(email);
                    Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

                    String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);

                    // 새 토큰 헤더에 추가
                    jwtTokenProvider.setHeaderAccessToken(response, newAccessToken);

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

    public static String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.AUTH_HEADER);
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
    public String resolveRefreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader("refreshToken");

        if (StringUtils.hasText(refreshToken)) {
            if (refreshToken.startsWith("Bearer ")) {
                return refreshToken.substring(7);
            }
            return refreshToken; // "Bearer " 없이도 허용
        }
        return null;
    }
}