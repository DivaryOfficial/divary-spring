package com.divary.global.oauth.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplePublicKey {
    private String kty; // Key Type
    private String kid; // Key ID
    private String use; // 사용 용도 (e.g., "sig")
    private String alg; // 알고리즘 (e.g., "RS256")
    private String n;   // Modulus
    private String e;   // Exponent
}