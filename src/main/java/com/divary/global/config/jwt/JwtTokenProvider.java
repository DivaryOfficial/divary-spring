package com.divary.global.config.jwt;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.device_session.repository.DeviceSessionRepository;
import com.divary.global.config.properties.JwtProperties;
import com.divary.global.config.security.CustomUserDetailsService;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;
    private final DeviceSessionRepository refreshTokenRepository;
    private final MemberRepository memberRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String userId = String.valueOf(principal.getId());

        return Jwts.builder()
                .setSubject(userId)
                .claim("role", authentication.getAuthorities().iterator().next().getAuthority())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
        String userId = String.valueOf(principal.getId());

        return Jwts.builder()
                .setSubject(userId)
                .claim("role", authentication.getAuthorities().iterator().next().getAuthority())
                .claim("type", "refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getRefresh()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        }catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다.");
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = getClaimsFromToken(token);

        Long userId;
        try {
            userId = Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        Member member = memberRepository.findById(userId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        CustomUserPrincipal principal = new CustomUserPrincipal(member);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }


    // RefreshToken 존재유무 확인
    public boolean existsRefreshToken(String refreshToken, String deviceId) {
        return refreshTokenRepository.existsByRefreshTokenAndDeviceId(refreshToken, deviceId);
    }

    public Member getUserFromToken(String token) {

        Claims claims = getClaimsFromToken(token);

        Member user = memberRepository.findById(Long.parseLong(claims.getSubject())).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return user;
    }
    public Role getRoleFromToken(String token) {

        Claims claims = getClaimsFromToken(token);

        return Role.valueOf(claims.get("role", String.class));
    }
    public List<String> getRoles(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();
    }
    public void setHeaderTokens(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("refreshToken", "Bearer " + refreshToken);
    }
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByRefreshToken(token);
    }

    public Long getRemainingExpiration(String token) {
        try {
            Claims claims = getClaimsFromToken(token);

            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();

            // 현재 시간보다 만료 시간이 이전이라면 0을 반환
            return Math.max(0, expiration.getTime() - now);
        } catch (Exception e) {
            // 토큰이 유효하지 않은 경우 (이미 만료되었거나, 서명이 잘못된 경우 등)
            return 0L;
        }
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
