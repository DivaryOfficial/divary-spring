package com.divary.global.redis.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {

    private final RedisTemplate<String, Object> redisTemplate;

    private final String REDIS_BLACK_LIST_KEY = "tokenBlackList";

    //BlackList 내에 토큰을 추가합니다.
    @Override
    public void addTokenToList(String value) {
        redisTemplate.opsForList().rightPush(REDIS_BLACK_LIST_KEY, value);
    }

    //BlackList 내에 토큰이 존재하는지 여부를 확인합니다.
    @Override
    public boolean isContainToken(String value) {
        List<Object> allItems = redisTemplate.opsForList().range(REDIS_BLACK_LIST_KEY, 0, -1);
        return allItems.stream()
                .anyMatch(item -> item.equals(value));
    }

    //BlackList 항목을 모두 조회합니다.
    public List<Object> getTokenBlackList() {
        return redisTemplate.opsForList().range(REDIS_BLACK_LIST_KEY, 0, -1);
    }

    //BlackList 내에서 항목을 제거합니다.
    @Override
    public void removeToken(String value) {
        redisTemplate.opsForList().remove(REDIS_BLACK_LIST_KEY, 0, value);
    }
}
