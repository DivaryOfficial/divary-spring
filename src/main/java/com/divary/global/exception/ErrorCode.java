package com.divary.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 실제 사용되는 공통 에러들
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "잘못된 입력값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_002", "지원하지 않는 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "서버 내부 오류가 발생했습니다."),

    // 실제 사용되는 검증 에러들
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_001", "입력값 검증에 실패했습니다."),
    REQUIRED_FIELD_MISSING(HttpStatus.BAD_REQUEST, "VALIDATION_002", "필수 필드가 누락되었습니다."),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST, "VALIDATION_003", "토큰이 잘못되었습니다."),


    CARD_NOT_FOUND(HttpStatus.NOT_FOUND, "ENCYCLOPEDIA_001", "해당 카드에 대한 정보를 찾을 수 없습니다."),
    TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "ENCYCLOPEDIA_002", "존재하지 않는 종류입니다."),

    //맴버 관련
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "이메일을 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_002", "유저를 찾을 수 없습니다."),

    //소셜 로그인 관련
    GOOGLE_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "GOOGLE_001", "구글 유저를 찾을 수 없습니다"),

    //알림 관련
    NOTIFICAITION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_003", "해당 ID를 가진 사용자의 알림이 존재하지 않습니다"),

    // 채팅방 관련 에러코드
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_ROOM_001", "채팅방을 찾을 수 없습니다.");

    // TODO: 비즈니스 로직 개발하면서 필요한 에러코드들 추가
    private final HttpStatus status;
    private final String code;
    private final String message;
}