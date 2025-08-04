package com.divary.global.config.security.jwt;

import com.divary.domain.refresh.repository.RefreshTokenRepository;
import com.divary.global.config.properties.Constants;
import com.divary.global.config.properties.JwtProperties;
import com.divary.global.config.security.CustomUserDetailsService;
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
    private final RefreshTokenRepository refreshTokenRepository;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        String email = authentication.getName();

        return Jwts.builder()
                .setSubject(email)
                .claim("role", authentication.getAuthorities().iterator().next().getAuthority())
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().getAccess()))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Authentication authentication) {
        String email = authentication.getName();

        return Jwts.builder()
                .setSubject(email)
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
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        String email = claims.getSubject();
        
        try {
            // CustomUserPrincipal을 통해 사용자 정보 로드
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            return new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());
        } catch (BusinessException e) {
            // 사용자 정보를 찾을 수 없는 경우
            throw new BusinessException(ErrorCode.INVALID_USER_CONTEXT);
        }
    }


    // RefreshToken 존재유무 확인
    public boolean existsRefreshToken(String refreshToken) {
        return refreshTokenRepository.existsByRefreshToken(refreshToken);
    }

    public String getUserEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
    public List<String> getRoles(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .toList();
    }
    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader(Constants.AUTH_HEADER, Constants.TOKEN_PREFIX + accessToken);
    }
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByRefreshToken(token);
    }

}
