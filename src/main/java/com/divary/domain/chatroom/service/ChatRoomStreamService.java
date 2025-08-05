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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomStreamService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;
    private final MessageFactory messageFactory;
    private final ImageService imageService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // SSE 연결 상태 관리
    private final ConcurrentHashMap<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();
    private final AtomicLong connectionIdGenerator = new AtomicLong(0);

    public SseEmitter streamChatRoomMessage(ChatRoomMessageRequest request, Long userId) {
        // 고유 연결 ID 생성
        String connectionId = "conn_" + userId + "_" + connectionIdGenerator.incrementAndGet();
        
        // 5분 타임아웃으로 SseEmitter 생성
        SseEmitter emitter = new SseEmitter(300_000L);
        
        // 연결 추가 및 정리 콜백 설정
        activeConnections.put(connectionId, emitter);
        emitter.onCompletion(() -> cleanupConnection(connectionId));
        emitter.onTimeout(() -> cleanupConnection(connectionId));
        emitter.onError((error) -> cleanupConnection(connectionId));
        
        log.info("새 SSE 연결 생성: {} (활성 연결 수: {})", connectionId, activeConnections.size());
        
        try {
            // OpenAI 스트림 호출 (재시도 및 타임아웃 설정)
            Flux<String> streamFlux = openAIService.sendMessageStreamRaw(
                request.getMessage(), 
                request.getImage(), 
                null // TODO: 히스토리 처리 로직 추가 예정
            )
            .timeout(Duration.ofMinutes(3)) // OpenAI 응답 대기 최대 3분
            .retry(2) // 네트워크 오류 시 최대 2회 재시도
            .doOnError(error -> log.error("OpenAI 스트림 호출 실패 [{}]: {}", connectionId, error.getMessage()));
            
            // SSE 이벤트 처리
            processStreamEvents(streamFlux, emitter, connectionId);
            
        } catch (Exception e) {
            log.error("스트림 처리 중 오류 발생 [{}]: {}", connectionId, e.getMessage());
            sendErrorToClient(emitter, connectionId, "스트림 초기화 실패", e);
        }
        
        return emitter;
    }
    
    // OpenAI SSE 스트림을 파싱하여 클라이언트에 전송
    private void processStreamEvents(Flux<String> streamFlux, SseEmitter emitter, String connectionId) {
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
                        log.error("SSE 이벤트 전송 오류 [{}]: {}", connectionId, e.getMessage());
                        sendErrorToClient(emitter, connectionId, "이벤트 전송 실패", e);
                    }
                },
                error -> {
                    String errorType = error.getClass().getSimpleName();
                    log.error("스트림 오류 [{}] - {}: {}", connectionId, errorType, error.getMessage());
                    
                    // 오류 유형별 처리
                    if (error instanceof java.util.concurrent.TimeoutException) {
                        sendErrorToClient(emitter, connectionId, "OpenAI 응답 타임아웃", error);
                    } else if (error instanceof java.net.ConnectException) {
                        sendErrorToClient(emitter, connectionId, "OpenAI 서버 연결 실패", error);
                    } else {
                        sendErrorToClient(emitter, connectionId, "스트림 처리 오류", error);
                    }
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
                        log.info("스트림 정상 완료 [{}]", connectionId);
                    } catch (Exception e) {
                        log.error("스트림 완료 처리 오류 [{}]: {}", connectionId, e.getMessage());
                        sendErrorToClient(emitter, connectionId, "완료 처리 실패", e);
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
    
    // 클라이언트에 에러 정보 전송
    private void sendErrorToClient(SseEmitter emitter, String connectionId, String errorType, Throwable error) {
        try {
            Map<String, Object> errorResponse = Map.of(
                "error", true,
                "errorType", errorType,
                "message", error.getMessage() != null ? error.getMessage() : "알 수 없는 오류",
                "timestamp", System.currentTimeMillis()
            );
            
            emitter.send(SseEmitter.event()
                .name("error")
                .data(errorResponse));
            
            log.info("에러 정보 전송 완료 [{}]: {}", connectionId, errorType);
        } catch (Exception e) {
            log.error("에러 정보 전송 실패 [{}]: {}", connectionId, e.getMessage());
        } finally {
            if (error instanceof Exception) {
                emitter.completeWithError((Exception) error);
            } else {
                emitter.completeWithError(new RuntimeException(error));
            }
            cleanupConnection(connectionId);
        }
    }
    
    // SSE 연결 정리 메서드
    private void cleanupConnection(String connectionId) {
        SseEmitter removed = activeConnections.remove(connectionId);
        if (removed != null) {
            log.info("연결 정리 완료: {} (남은 활성 연결 수: {})", connectionId, activeConnections.size());
        }
    }
    
    // 활성 연결 수 조회 (모니터링용)
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }
    
    // 특정 연결 강제 종료 (관리용)
    public boolean forceCloseConnection(String connectionId) {
        SseEmitter emitter = activeConnections.get(connectionId);
        if (emitter != null) {
            try {
                emitter.complete();
                cleanupConnection(connectionId);
                log.info("연결 강제 종료: {}", connectionId);
                return true;
            } catch (Exception e) {
                log.error("연결 강제 종료 실패 [{}]: {}", connectionId, e.getMessage());
                cleanupConnection(connectionId);
                return false;
            }
        }
        return false;
    }
}
