package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.response.ChatStreamResponseDto;
import com.divary.domain.chatroom.repository.ChatRoomRepository;
import com.divary.domain.image.service.ImageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomStreamService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;
    private final MessageFactory messageFactory;
    private final ImageService imageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SseEmitter streamChatRoomMessage(ChatRoomMessageRequest request, Long userId) {
        // 5분 타임아웃으로 SseEmitter 생성
        SseEmitter emitter = new SseEmitter(300_000L);
        
        try {
            // OpenAI 스트림 호출
            Flux<String> streamFlux = openAIService.sendMessageStreamRaw(
                request.getMessage(), 
                request.getImage(), 
                null // TODO: 히스토리 처리 로직 추가 예정
            );
            
            // SSE 이벤트 처리
            processStreamEvents(streamFlux, emitter);
            
        } catch (Exception e) {
            log.error("스트림 처리 중 오류 발생: {}", e.getMessage());
            emitter.completeWithError(e);
        }
        
        return emitter;
    }
    
    // OpenAI SSE 스트림을 파싱하여 클라이언트에 전송
    private void processStreamEvents(Flux<String> streamFlux, SseEmitter emitter) {
        StringBuilder messageBuilder = new StringBuilder();
        
        streamFlux
            .filter(chunk -> !chunk.trim().isEmpty())
            .subscribe(
                chunk -> {
                    try {
                        String content = parseSSEChunk(chunk);
                        if (content != null && !content.isEmpty()) {
                            messageBuilder.append(content);
                            
                            // 클라이언트에 진행상황 전송
                            Map<String, Object> response = Map.of(
                                "content", content,
                                "isComplete", false,
                                "accumulatedMessage", messageBuilder.toString()
                            );
                            
                            emitter.send(SseEmitter.event()
                                .name("message")
                                .data(response));
                        }
                    } catch (Exception e) {
                        log.error("SSE 이벤트 전송 오류: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("스트림 오류: {}", error.getMessage());
                    emitter.completeWithError(error);
                },
                () -> {
                    try {
                        // 스트림 완료 시 최종 메시지 전송
                        Map<String, Object> finalResponse = Map.of(
                            "content", "",
                            "isComplete", true,
                            "accumulatedMessage", messageBuilder.toString()
                        );
                        
                        emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(finalResponse));
                        
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("스트림 완료 처리 오류: {}", e.getMessage());
                        emitter.completeWithError(e);
                    }
                }
            );
    }
    
    // OpenAI SSE 청크 파싱 - "data: {json}" 형태 처리
    private String parseSSEChunk(String sseChunk) {
        try {
            if (sseChunk.startsWith("data: ")) {
                String jsonData = sseChunk.substring(6).trim();
                
                if ("[DONE]".equals(jsonData)) {
                    return null; // 스트림 종료 신호
                }
                
                JsonNode jsonNode = objectMapper.readTree(jsonData);
                JsonNode choices = jsonNode.path("choices");
                
                if (choices.isArray() && choices.size() > 0) {
                    JsonNode delta = choices.get(0).path("delta");
                    JsonNode content = delta.path("content");
                    
                    if (!content.isMissingNode() && content.isTextual()) {
                        return content.asText();
                    }
                }
            }
            return null;
        } catch (Exception e) {
            log.error("SSE 청크 파싱 오류: {}", e.getMessage());
            return null;
        }
    }
}
