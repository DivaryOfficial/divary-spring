package com.divary.domain.chatroom.service;

import com.divary.global.config.OpenAIConfig;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 토큰 사용량 추적 및 제한 서비스
 * Redis를 사용하여 사용자별 토큰 사용량을 추적하고 제한합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenUsageService {

    private final RedisTemplate<String, String> redisTemplate;
    private final OpenAIConfig openAIConfig;

    /**
     * 토큰 사용 가능 여부 확인 및 사용량 기록
     *
     * @param userId 사용자 ID
     * @param estimatedTokens 예상 토큰 수
     * @throws BusinessException 토큰 제한 초과 시
     */
    public void checkAndRecordUsage(Long userId, int estimatedTokens) {
        String dailyKey = buildDailyKey(userId);
        String monthlyKey = buildMonthlyKey(userId);

        // 일일 사용량 확인
        int dailyUsage = getUsage(dailyKey);
        int dailyLimit = openAIConfig.getUsageLimits().getPerUserDaily();

        if (dailyUsage + estimatedTokens > dailyLimit) {
            log.warn("일일 토큰 제한 초과 - 사용자: {}, 현재: {}, 요청: {}, 제한: {}",
                userId, dailyUsage, estimatedTokens, dailyLimit);
            throw new BusinessException(ErrorCode.DAILY_TOKEN_LIMIT_EXCEEDED);
        }

        // 월간 사용량 확인
        int monthlyUsage = getUsage(monthlyKey);
        int monthlyLimit = openAIConfig.getUsageLimits().getPerUserMonthly();

        if (monthlyUsage + estimatedTokens > monthlyLimit) {
            log.warn("월간 토큰 제한 초과 - 사용자: {}, 현재: {}, 요청: {}, 제한: {}",
                userId, monthlyUsage, estimatedTokens, monthlyLimit);
            throw new BusinessException(ErrorCode.MONTHLY_TOKEN_LIMIT_EXCEEDED);
        }

        // 사용량 기록
        recordUsage(dailyKey, monthlyKey, estimatedTokens);

        log.info("토큰 사용량 기록 - 사용자: {}, 토큰: {}, 일일 누적: {}/{}, 월간 누적: {}/{}",
            userId, estimatedTokens, dailyUsage + estimatedTokens, dailyLimit,
            monthlyUsage + estimatedTokens, monthlyLimit);
    }

    /**
     * 실제 사용한 토큰 수로 사용량 업데이트
     * API 응답 후 실제 사용량으로 정확하게 업데이트
     *
     * @param userId 사용자 ID
     * @param estimatedTokens 예상 토큰 수 (차감할 값)
     * @param actualTokens 실제 사용 토큰 수 (추가할 값)
     */
    public void updateActualUsage(Long userId, int estimatedTokens, int actualTokens) {
        String dailyKey = buildDailyKey(userId);
        String monthlyKey = buildMonthlyKey(userId);

        // 예상값 차감
        redisTemplate.opsForValue().decrement(dailyKey, estimatedTokens);
        redisTemplate.opsForValue().decrement(monthlyKey, estimatedTokens);

        // 실제값 추가
        redisTemplate.opsForValue().increment(dailyKey, actualTokens);
        redisTemplate.opsForValue().increment(monthlyKey, actualTokens);

        log.debug("토큰 사용량 업데이트 - 사용자: {}, 예상: {}, 실제: {}", userId, estimatedTokens, actualTokens);
    }

    /**
     * 사용자의 남은 토큰 조회
     *
     * @param userId 사용자 ID
     * @return 일일/월간 남은 토큰 정보
     */
    public UsageInfo getRemainingTokens(Long userId) {
        String dailyKey = buildDailyKey(userId);
        String monthlyKey = buildMonthlyKey(userId);

        int dailyUsed = getUsage(dailyKey);
        int monthlyUsed = getUsage(monthlyKey);

        int dailyLimit = openAIConfig.getUsageLimits().getPerUserDaily();
        int monthlyLimit = openAIConfig.getUsageLimits().getPerUserMonthly();

        return UsageInfo.builder()
                .dailyUsed(dailyUsed)
                .dailyRemaining(Math.max(0, dailyLimit - dailyUsed))
                .dailyLimit(dailyLimit)
                .monthlyUsed(monthlyUsed)
                .monthlyRemaining(Math.max(0, monthlyLimit - monthlyUsed))
                .monthlyLimit(monthlyLimit)
                .build();
    }

    /**
     * Redis에서 사용량 조회
     */
    private int getUsage(String key) {
        String value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value) : 0;
    }

    /**
     * Redis에 사용량 기록
     */
    private void recordUsage(String dailyKey, String monthlyKey, int tokens) {
        // 사용량 증가
        redisTemplate.opsForValue().increment(dailyKey, tokens);
        redisTemplate.opsForValue().increment(monthlyKey, tokens);

        // 만료 시간 설정 (키가 처음 생성될 때만)
        Boolean dailyExists = redisTemplate.hasKey(dailyKey);
        if (Boolean.TRUE.equals(dailyExists)) {
            redisTemplate.expire(dailyKey, Duration.ofDays(1));
        }

        Boolean monthlyExists = redisTemplate.hasKey(monthlyKey);
        if (Boolean.TRUE.equals(monthlyExists)) {
            redisTemplate.expire(monthlyKey, Duration.ofDays(31));
        }
    }

    /**
     * Redis 키 생성 - 일일
     */
    private String buildDailyKey(Long userId) {
        return "token:usage:user:" + userId + ":daily:" + LocalDate.now();
    }

    /**
     * Redis 키 생성 - 월간
     */
    private String buildMonthlyKey(Long userId) {
        return "token:usage:user:" + userId + ":monthly:" + YearMonth.now();
    }

    /**
     * 사용량 정보 DTO
     */
    @lombok.Builder
    @lombok.Getter
    public static class UsageInfo {
        private int dailyUsed;
        private int dailyRemaining;
        private int dailyLimit;
        private int monthlyUsed;
        private int monthlyRemaining;
        private int monthlyLimit;
    }
}
