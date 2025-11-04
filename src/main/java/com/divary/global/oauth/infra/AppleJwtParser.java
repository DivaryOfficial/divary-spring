package com.divary.global.oauth.infra;

import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.ApplePublicKey;
import com.divary.global.oauth.dto.response.ApplePublicKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AppleJwtParser {

    private static final String APPLE_PUBLIC_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String APPLE_CLIENT_ID = "io.tuist.Divary"; // TODO property로 변경

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Apple Identity Token을 검증하고 사용자 정보를 추출합니다.
     * @param identityToken 클라이언트로부터 받은 Identity Token
     * @return 사용자 정보 (sub: Apple User ID, email: 이메일 또는 빈 문자열)
     */
    public Map<String, String> parse(String identityToken) {
        // 1. Apple 공개키 목록을 가져옵니다. (실제 운영에서는 캐싱 필요)
        ApplePublicKeys publicKeys = getApplePublicKeys();

        // 2. 토큰의 헤더를 디코딩하여 kid와 alg를 찾습니다.
        String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));
        Map<String, String> header;
        try {
            header = objectMapper.readValue(new String(Base64.getUrlDecoder().decode(headerOfIdentityToken), "UTF-8"), Map.class);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Identity Token 헤더 파싱에 실패했습니다.");
        }
        String tokenKid = header.get("kid");
        String tokenAlg = header.get("alg");

        // 3. 토큰의 kid, alg와 일치하는 공개키를 찾습니다.
        ApplePublicKey matchedKey = publicKeys.getKeys().stream()
                .filter(key -> Objects.equals(key.getKid(), tokenKid) && Objects.equals(key.getAlg(), tokenAlg))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN, "일치하는 Apple 공개키를 찾을 수 없습니다."));

        // 4. 찾은 공개키로 실제 PublicKey 객체를 생성합니다.
        PublicKey publicKey = generatePublicKey(matchedKey);

        // 5. 생성된 PublicKey로 토큰의 서명, 발급자, 만료시간 등을 최종 검증합니다.
        Claims claims = getClaims(identityToken, publicKey);

        String sub = claims.getSubject();
        String email = claims.get("email", String.class);

        return Map.of(
            "sub", sub,
            "email", email != null ? email : ""
        );
    }

    /**
     * Apple 공개키 서버에서 키 목록을 가져옵니다.
     */
    private ApplePublicKeys getApplePublicKeys() {
        try {
            String jsonResponse = restTemplate.getForObject(APPLE_PUBLIC_KEYS_URL, String.class);
            return objectMapper.readValue(jsonResponse, ApplePublicKeys.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Apple 공개키를 가져오거나 파싱하는 데 실패했습니다.", e);
        }
    }

    /**
     * JWK(n, e) 값으로 PublicKey 객체를 생성합니다.
     */
    private PublicKey generatePublicKey(ApplePublicKey applePublicKey) {
        byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.getE());

        BigInteger n = new BigInteger(1, nBytes);
        BigInteger e = new BigInteger(1, eBytes);

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(applePublicKey.getKty());
            return keyFactory.generatePublic(publicKeySpec);
        } catch (Exception ex) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN, "Apple 공개키 생성에 실패했습니다.");
        }
    }

    /**
     * PublicKey를 사용하여 Identity Token을 최종 검증하고 Claims를 반환합니다.
     */
    private Claims getClaims(String identityToken, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(APPLE_CLIENT_ID)
                    .build()
                    .parseClaimsJws(identityToken)
                    .getBody();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
