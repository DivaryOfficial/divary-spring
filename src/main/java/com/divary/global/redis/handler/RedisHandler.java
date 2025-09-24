package com.divary.global.redis.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHandler {

    // ⭐️ 1. RedisConfig 의존성 제거, RedisTemplate만 주입받습니다.
    private final RedisTemplate<String, Object> redisTemplate;

    public ListOperations<String, Object> getListOperations() {
        // ⭐️ 주입받은 redisTemplate 필드를 일관되게 사용합니다.
        return redisTemplate.opsForList();
    }

    public ValueOperations<String, Object> getValueOperations() {
        // ⭐️ 주입받은 redisTemplate 필드를 일관되게 사용합니다.
        return redisTemplate.opsForValue();
    }

    // ⭐️ 2. delete 메서드 수정 (컴파일 오류 해결)
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }

    /**
     * 반환 값이 없는 Redis 작업을 처리합니다. (예: set)
     */
    public void execute(Runnable operation) {
        try {
            operation.run();
        } catch (Exception e) {
            // ⭐️ 3. SLF4J 로거를 사용하고, 예외를 던져서 호출부가 알 수 있도록 합니다.
            log.error("Redis 작업 오류 발생", e);
            throw new RuntimeException("Redis 작업 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 반환 값이 있는 Redis 작업을 처리합니다. (예: delete)
     * @return 작업의 결과
     */
    public <T> T execute(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (Exception e) {
            log.error("Redis 작업 오류 발생", e);
            throw new RuntimeException("Redis 작업 중 오류가 발생했습니다.", e);
        }
    }
}