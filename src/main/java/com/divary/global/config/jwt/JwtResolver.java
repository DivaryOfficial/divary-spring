package com.divary.global.config.jwt;

import com.divary.global.config.properties.Constants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtResolver {
    public String resolveAccessToken(HttpServletRequest request) {
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
