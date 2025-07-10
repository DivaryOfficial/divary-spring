package com.divary.domain.chatroom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateTitle(String userMessage) {
        try {
            String titlePrompt = """
                You are a system that generates concise and clear chat titles for a marine observation log app.
                
                Your task is to create a short title that summarizes the user's first message about a marine creature.
                
                Follow these strict rules when generating the title:
                The title must be in Korean.
                Maximum 30 characters, including spaces.
                Do not use verbs like "발견", "봤어요", "있었어요".
                Use descriptive keywords only: observed location (e.g., near anemone, on a rock), appearance (e.g., yellow stripes, transparent body), behavior (e.g., slowly moving, stuck to the ground).
                Remove all emotional expressions, emojis, and exclamations.
                Output should be a noun phrase only — no full sentences.
                Focus on how the user described the creature, not on guessing the actual species name.
                
                Examples:
                
                Input: "노란 줄무늬 생물을 말미잘 옆에서 봤어요! 움직이고 있었어요!"
                Output: "말미잘 옆 노란 줄무늬 생물"
                
                Input: "돌 위에 빨간 생물이 있었어요. 별처럼 생겼어요"
                Output: "돌 위 별 모양 생물"
                
                Now generate the title for this message:
                
                "{USER_MESSAGE}"
                """.replace("{USER_MESSAGE}", userMessage);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 50);
            requestBody.put("temperature", 0.3);

            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", titlePrompt);

            requestBody.put("messages", List.of(systemMessage));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(apiUrl, request, String.class);
            
            JsonNode jsonNode = objectMapper.readTree(response);
            String generatedTitle = jsonNode.path("choices").get(0).path("message").path("content").asText().trim();
            
            // 30자 제한 확인
            if (generatedTitle.length() > 30) {
                generatedTitle = generatedTitle.substring(0, 27) + "...";
            }
            
            return generatedTitle;
        } catch (Exception e) {
            log.error("제목 생성 중 오류 발생: {}", e.getMessage());
            return generateFallbackTitle(userMessage);
        }
    }

    private String generateFallbackTitle(String message) {
        if (message == null || message.trim().isEmpty()) {
            return "새 채팅방";
        }
        
        String cleanMessage = message.replaceAll("[\\r\\n\\t]", " ")
                                .replaceAll("\\s+", " ")
                                .trim();
        
        if (cleanMessage.length() <= 30) {
            return cleanMessage;
        } else {
            return cleanMessage.substring(0, 27) + "...";
        }
    }

    public OpenAIResponse sendMessage(String message, MultipartFile imageFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 1000);
            requestBody.put("temperature", 0.7);

            // 메시지 구성
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            
            if (imageFile != null && !imageFile.isEmpty()) {
                // 이미지와 텍스트가 모두 있는 경우
                String base64Image = encodeImageToBase64(imageFile);
                String mimeType = imageFile.getContentType();
                
                List<Map<String, Object>> content = List.of(
                    Map.of("type", "text", "text", message),
                    Map.of("type", "image_url", "image_url", 
                        Map.of("url", "data:" + mimeType + ";base64," + base64Image))
                );
                userMessage.put("content", content);
            } else {
                // 텍스트만 있는 경우
                userMessage.put("content", message);
            }

            requestBody.put("messages", List.of(userMessage));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(apiUrl, request, String.class);
            
            return parseResponse(response);
        } catch (Exception e) {
            log.error("OpenAI API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("OpenAI API 호출 실패", e);
        }
    }

    private OpenAIResponse parseResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            
            String content = jsonNode.path("choices").get(0).path("message").path("content").asText();
            
            JsonNode usage = jsonNode.path("usage");
            int promptTokens = usage.path("prompt_tokens").asInt();
            int completionTokens = usage.path("completion_tokens").asInt();
            int totalTokens = usage.path("total_tokens").asInt();
            
            // 비용 계산 (gpt-4o-mini 기준)
            double cost = calculateCost(promptTokens, completionTokens);
            
            return OpenAIResponse.builder()
                    .content(content)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .model(model)
                    .cost(cost)
                    .build();
        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("OpenAI 응답 파싱 실패", e);
        }
    }

    private double calculateCost(int promptTokens, int completionTokens) {
        // gpt-4o-mini 가격 (2024년 기준)
        double inputCostPer1K = 0.00015;  // $0.00015 per 1K tokens
        double outputCostPer1K = 0.0006;  // $0.0006 per 1K tokens
        
        return (promptTokens * inputCostPer1K / 1000) + (completionTokens * outputCostPer1K / 1000);
    }

    private String encodeImageToBase64(MultipartFile imageFile) {
        try {
            byte[] imageBytes = imageFile.getBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("이미지 Base64 인코딩 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("이미지 인코딩 실패", e);
        }
    }

    public static class OpenAIResponse {
        private String content;
        private int promptTokens;
        private int completionTokens;
        private int totalTokens;
        private String model;
        private double cost;

        public static OpenAIResponseBuilder builder() {
            return new OpenAIResponseBuilder();
        }

        public String getContent() { return content; }
        public int getPromptTokens() { return promptTokens; }
        public int getCompletionTokens() { return completionTokens; }
        public int getTotalTokens() { return totalTokens; }
        public String getModel() { return model; }
        public double getCost() { return cost; }

        public static class OpenAIResponseBuilder {
            private String content;
            private int promptTokens;
            private int completionTokens;
            private int totalTokens;
            private String model;
            private double cost;

            public OpenAIResponseBuilder content(String content) {
                this.content = content;
                return this;
            }

            public OpenAIResponseBuilder promptTokens(int promptTokens) {
                this.promptTokens = promptTokens;
                return this;
            }

            public OpenAIResponseBuilder completionTokens(int completionTokens) {
                this.completionTokens = completionTokens;
                return this;
            }

            public OpenAIResponseBuilder totalTokens(int totalTokens) {
                this.totalTokens = totalTokens;
                return this;
            }

            public OpenAIResponseBuilder model(String model) {
                this.model = model;
                return this;
            }

            public OpenAIResponseBuilder cost(double cost) {
                this.cost = cost;
                return this;
            }

            public OpenAIResponse build() {
                OpenAIResponse response = new OpenAIResponse();
                response.content = this.content;
                response.promptTokens = this.promptTokens;
                response.completionTokens = this.completionTokens;
                response.totalTokens = this.totalTokens;
                response.model = this.model;
                response.cost = this.cost;
                return response;
            }
        }
    }
}