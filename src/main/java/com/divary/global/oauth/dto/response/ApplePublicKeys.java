package com.divary.global.oauth.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class ApplePublicKeys {
    private List<ApplePublicKey> keys;
}
