package com.divary.domain.image.entity;

import lombok.Getter;

@Getter
public enum ImageType {
    // 유저 이미지
    USER_DIVING_LOG("users/{userId}/logbook/{datePath}/{fileName}", true),
    USER_CHAT("users/{userId}/chat/{fileName}", true),
    USER_LICENSE("users/{userId}/license/{fileName}", true),
    
    // 시스템 이미지 (모든 유저에게 공통인 것들을 처리하면 됩니다.)
    SYSTEM_DOGAM("system/dogam/{type}/{fileName}", false),
    SYSTEM_DOGAM_PROFILE("system/dogam/profile/{fileName}", false);

    private final String pathPattern;
    private final boolean requiresUserId;

    ImageType(String pathPattern, boolean requiresUserId) {
        this.pathPattern = pathPattern;
        this.requiresUserId = requiresUserId;
    }
}