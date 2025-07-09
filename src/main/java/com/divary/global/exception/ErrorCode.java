package com.divary.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 실제 사용되는 공통 에러들ㄷ
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),

    // 실제 사용되는 검증 에러들
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_001", "입력값 검증에 실패했습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "VALIDATION_002", "필수 필드가 누락되었습니다."),

    // TODO: 비즈니스 로직 개발하면서 필요한 에러코드들 추가
    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ENCYCLOPEDIA_001", "해당 카드에 대한 정보를 찾을 수 없습니다."),
    TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENCYCLOPEDIA_002", "존재하지 않는 종류입니다."),
    CARD_APPEARANCE_NOT_FOUND(HttpStatus.NOT_FOUND,"APPEARANCE_NOT_FOUND", "해당 생물에 대한 외모 정보가 아직 없습니다."),
    CARD_PERSONALITY_NOT_FOUND(HttpStatus.NOT_FOUND, "PERSONALITY_NOT_FOUND", "해당 생물에 대한 성격 정보가 아직 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}