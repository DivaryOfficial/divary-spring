package com.divary.domain.image.entity;

import lombok.Getter;

@Getter
public enum ImageType {
    // 유저 이미지
    USER_DIVING_LOG,
    USER_CHAT,
    USER_LICENSE,
    
    // 시스템 이미지 (모든 유저에게 공통인 것들을 처리하면 됩니다.)
    SYSTEM_DOGAM,           
    SYSTEM_DOGAM_PROFILE;
}