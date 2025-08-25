package com.divary.global.redis.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Duration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RedisDto {

    @Schema(description = "Redis 키", example = "auth:bl:abc-123")
    @NotBlank(message = "key는 필수입니다.")
    private String key;

    @Schema(description = "저장할 값 (get/delete에선 생략 가능)", example = "some-value")
    private String value;

    @Schema(
            description = "유효시간(ISO-8601 Duration). 예: PT30S(30초), PT5M(5분). 미설정 시 TTL 없음",
            example = "PT30S"
    )
    private Duration duration;
}