package com.divary.global.oauth.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeactivateResponse {
    // 최종 삭제 예정 시간을 담을 필드
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime scheduledDeletionAt;

    public DeactivateResponse(LocalDateTime scheduledDeletionAt) {
        this.scheduledDeletionAt = scheduledDeletionAt;
    }
}
