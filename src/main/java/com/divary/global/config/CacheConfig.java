package com.divary.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    public static final String CACHE_MEMBER_BY_ID = "member:byId";
    public static final String CACHE_CHATROOM_DETAIL = "chatroom:detail";
    public static final String CACHE_CHATROOMS_BY_USER = "chatroom:listByUser";
    public static final String CACHE_ENCYCLOPEDIA_SUMMARY = "encyclopedia:summary";
    public static final String CACHE_ENCYCLOPEDIA_DETAIL = "encyclopedia:detail";
    public static final String CACHE_IMAGES_BY_PATH = "image:byPath";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        Caffeine<Object, Object> shortTtl = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(5_000);

        Caffeine<Object, Object> mediumTtl = Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(10_000);

        Caffeine<Object, Object> longTtl = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(20_000);

        cacheManager.setCaches(Arrays.asList(
                new CaffeineCache(CACHE_MEMBER_BY_ID, mediumTtl.build()),
                new CaffeineCache(CACHE_CHATROOM_DETAIL, shortTtl.build()),
                new CaffeineCache(CACHE_CHATROOMS_BY_USER, shortTtl.build()),
                new CaffeineCache(CACHE_ENCYCLOPEDIA_SUMMARY, longTtl.build()),
                new CaffeineCache(CACHE_ENCYCLOPEDIA_DETAIL, longTtl.build()),
                new CaffeineCache(CACHE_IMAGES_BY_PATH, mediumTtl.build())
        ));

        return cacheManager;
    }
}


