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
    private String chatCompletionsUrl;
    private String responsesUrl;
    
    // GPT-5-nano 최적화 파라미터
    private ReasoningConfig reasoning = new ReasoningConfig();
    private TextConfig text = new TextConfig();
    
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