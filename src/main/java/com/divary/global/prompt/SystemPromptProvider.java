package com.divary.global.prompt;

import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class SystemPromptProvider {

    private static final String MARINE_DIVING_PROMPT_PATH = "prompts/marine_diving_prompt.txt";
    private static final String TITLE_PROMPT_PATH = "prompts/title_prompt.txt";

    private String marineDivingPrompt;
    private String titlePromptTemplate;

    @PostConstruct
    void loadPrompts() {
        try {
            ClassPathResource marine = new ClassPathResource(MARINE_DIVING_PROMPT_PATH);
            byte[] marineBytes = marine.getInputStream().readAllBytes();
            this.marineDivingPrompt = new String(marineBytes, StandardCharsets.UTF_8);

            ClassPathResource title = new ClassPathResource(TITLE_PROMPT_PATH);
            byte[] titleBytes = title.getInputStream().readAllBytes();
            this.titlePromptTemplate = new String(titleBytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            // Fallback: 최소 안전 프롬프트
            this.marineDivingPrompt = "You are DIVING_MARINE_EXPERT. Answer in Korean. If topic is not marine or diving, reply: \"죄송합니다. 다이빙과 해양생물 전문 서비스 정책상 해당 질문에는 답변드릴 수 없습니다.\"";
            this.titlePromptTemplate = "You generate short Korean titles about a marine creature. Max 30 chars. Input: \"{{USER_MESSAGE}}\"";
        }
    }

    public String getMarineDivingPrompt() {
        return marineDivingPrompt;
    }

    public String buildTitlePrompt(String userMessage) {
        if (titlePromptTemplate == null || titlePromptTemplate.isEmpty()) {
            return "You generate short Korean titles about a marine creature. Max 30 chars. Input: \"" + userMessage + "\"";
        }
        return titlePromptTemplate.replace("{{USER_MESSAGE}}", userMessage);
    }
}


