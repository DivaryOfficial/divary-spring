package com.divary.global.redis.service;

import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public void addToBlacklist(String token) {
        Long remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);

        if (remainingExpiration > 0) {
            stringRedisTemplate.opsForValue().set(
                    token,
                    "logout",
                    remainingExpiration,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    @Override
    public boolean isContainToken(String token) {
        return stringRedisTemplate.hasKey(token);
    }


    @Override
    public void removeToken(String token) {
        stringRedisTemplate.delete(token);
    }
}