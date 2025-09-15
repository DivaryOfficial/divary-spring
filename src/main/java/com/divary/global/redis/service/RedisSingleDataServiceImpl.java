package com.divary.global.redis.service;

import com.divary.global.redis.handler.RedisHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.Duration;

/**
 * Redis의 단일 Key-Value 데이터를 처리하는 서비스 구현체입니다.
 * 모든 Redis 작업은 RedisHandler를 통해 일관된 방식으로 처리됩니다.
 */
@Service
@RequiredArgsConstructor
public class RedisSingleDataServiceImpl implements RedisSingleDataService {

    private final RedisHandler redisHandler;

    /**
     * Redis에 데이터를 등록합니다 (만료 시간 없음).
     * 작업 실패 시 RedisHandler에서 RuntimeException이 발생합니다.
     *
     * @param key   저장할 데이터의 키
     * @param value 저장할 데이터의 값 (객체)
     */
    @Override
    public void setSingleData(String key, Object value) {
        // ⭐️ 반환 값이 없는 Runnable을 사용하는 execute 메서드 호출
        redisHandler.execute(() -> redisHandler.getValueOperations().set(key, value));
    }

    /**
     * Redis에 데이터를 등록하고, 만료 시간(TTL)을 설정합니다.
     * 작업 실패 시 RedisHandler에서 RuntimeException이 발생합니다.
     *
     * @param key      저장할 데이터의 키
     * @param value    저장할 데이터의 값 (객체)
     * @param duration 데이터의 유효 시간 (TTL)
     */
    @Override
    public void setSingleData(String key, Object value, Duration duration) {
        // ⭐️ 반환 값이 없는 Runnable을 사용하는 execute 메서드 호출
        redisHandler.execute(() -> redisHandler.getValueOperations().set(key, value, duration));
    }

    /**
     * 키에 해당하는 데이터를 조회합니다.
     *
     * @param key 조회할 데이터의 키
     * @return 조회된 데이터의 값 (String). 키가 존재하지 않으면 null을 반환합니다.
     */
    @Override
    public String getSingleData(String key) {
        // 이 메서드는 예외 처리 핸들링이 필요 없으므로 직접 호출
        Object value = redisHandler.getValueOperations().get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 키에 해당하는 데이터를 삭제합니다.
     *
     * @param key 삭제할 데이터의 키
     * @return 삭제 성공 시 true, 실패 시 false
     */
    @Override
    public boolean deleteSingleData(String key) {
        // ⭐️ boolean 값을 반환하는 Supplier를 사용하는 execute 메서드 호출
        return redisHandler.execute(() -> redisHandler.delete(key));
    }
}