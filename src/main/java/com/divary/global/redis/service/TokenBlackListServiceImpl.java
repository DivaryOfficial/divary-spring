package com.divary.global.redis.service;

import com.divary.global.config.jwt.JwtTokenProvider;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class TokenBlackListServiceImpl implements TokenBlackListService {

    private final RedisSingleDataService redisSingleDataService;
    private final JwtTokenProvider jwtTokenProvider;



    @Override
    public void addToBlacklist(String token) {
        Long remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);

        if (remainingExpiration > 0) {
            redisSingleDataService.setSingleData(token, "logout", Duration.ofMillis(remainingExpiration));
        }
    }

    @Override
    public boolean isContainToken(String token) {
        return redisSingleDataService.hasSingleData(token);

    }


    @Override
    public void removeToken(String token) {
        redisSingleDataService.deleteSingleData(token);
    }
}