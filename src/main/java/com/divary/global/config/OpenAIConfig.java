package com.divary.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI API 설정 클래스
 * GPT-5-nano 모델 및 향후 Responses API 마이그레이션을 위한 설정
 */
@Configuration
@ConfigurationProperties(prefix = "openai.api")
@Getter
@Setter
public class OpenAIConfig {

    private String key;
    private String model;
    private String baseUrl;
    private String chatCompletionsUrl;
    private String responsesUrl;
    private String responsesEndpoint;

    // GPT-5-nano 최적화 파라미터
    private ReasoningConfig reasoning = new ReasoningConfig();
    private TextConfig text = new TextConfig();

    // 토큰 제한 설정
    private TokenLimits tokenLimits = new TokenLimits();

    // 사용량 제한 설정
    private UsageLimits usageLimits = new UsageLimits();
    
    @Getter
    @Setter
    public static class ReasoningConfig {
        private String effort = "minimal"; // minimal, low, medium, high
    }
    
    @Getter
    @Setter
    public static class TextConfig {
        private String verbosity = "low"; // low, medium, high
    }

    /**
     * 토큰 제한 설정 (응답 크기 제한)
     */
    @Getter
    @Setter
    public static class TokenLimits {
        private LimitConfig titleGeneration = new LimitConfig();
        private LimitConfig messageResponse = new LimitConfig();
    }

    /**
     * 개별 제한 설정
     */
    @Getter
    @Setter
    public static class LimitConfig {
        private int maxOutput;
        private int maxInput;
    }

    /**
     * 사용량 제한 설정 (비용 제한)
     */
    @Getter
    @Setter
    public static class UsageLimits {
        private int perUserDaily;
        private int perUserMonthly;
        private int totalDaily;
        private int totalMonthly;
    }

    /**
     * GPT-5-nano는 고처리량 작업에 최적화된 모델
     * - 빠른 응답 시간
     * - 단순 지시 수행에 특화
     * - 분류 작업에 우수한 성능
     */
    public boolean isGpt5Model() {
        return model != null && model.startsWith("gpt-5");
    }

    /**
     * Responses API 사용 가능 여부 확인
     * GPT-5 모델군은 Responses API에서 더 나은 성능 제공
     */
    public boolean supportsResponsesApi() {
        return isGpt5Model() && responsesUrl != null;
    }
}