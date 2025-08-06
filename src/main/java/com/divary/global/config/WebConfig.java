package com.divary.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.divary.global.intercepter.LoggingInterceptor;

// Interceptor를 Spring MVC에 등록, 특정 URL 경로에 대해 적용
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                // 인터셉터가 실행될 경로를 설정하는 필터
                .addPathPatterns("/**")
                // 인터셉터가 실행되지 않을 경로를 설정하는 필터
                .excludePathPatterns("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
    
    @Override
    public void configurePathMatch(@NonNull PathMatchConfigurer configurer) {
        // 모든 API 경로에 /api/v1 접두사 추가
        configurer.addPathPrefix("/api/v1", c -> c.isAnnotationPresent(org.springframework.web.bind.annotation.RestController.class));
    }
    
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/api/v1/chatrooms/stream")
                .allowedOriginPatterns("*") // iOS 앱 개발 환경 고려
                .allowedMethods("POST", "GET", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .exposedHeaders("Content-Type", "Cache-Control", "Connection") // SSE 필수 헤더 노출
                .maxAge(3600);
    }
} 