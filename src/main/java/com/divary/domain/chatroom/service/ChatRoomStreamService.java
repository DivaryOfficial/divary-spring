package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.entity.ChatRoom;
import com.divary.domain.chatroom.repository.ChatRoomRepository;
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.common.converter.TypeConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        
        // 스트림 시작 이벤트 전송 (히스토리 정보 포함)
        sendStreamStartEvent(emitter, connectionId, request);
        
        try {
            // 히스토리 처리 로직: chatRoomId가 있으면 기존 대화 내역을 OpenAI에 전달
            List<Map<String, Object>> messageHistory = null;
            if (request.getChatRoomId() != null) {
                messageHistory = buildMessageHistoryForOpenAI(request.getChatRoomId());
            }
            
            // OpenAI 스트림 호출 (재시도 및 타임아웃 설정)
            Flux<String> streamFlux = openAIService.sendMessageStreamRaw(
                request.getMessage(), 
                request.getImage(), 
                messageHistory
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
        AtomicLong chunkCounter = new AtomicLong(0);
        
        streamFlux
            .filter(chunk -> !chunk.trim().isEmpty())
            .subscribe(
                chunk -> {
                    try {
                        String content = parseSSEChunk(chunk);
                        if (content != null && !content.isEmpty()) {
                            messageBuilder.append(content);
                            long chunkIndex = chunkCounter.incrementAndGet();
                            
                            // 메시지 청크 이벤트 전송
                            sendMessageChunkEvent(emitter, connectionId, content, messageBuilder.toString(), chunkIndex);
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
                        // 스트림 완료 이벤트 전송
                        sendStreamCompleteEvent(emitter, connectionId, messageBuilder.toString(), chunkCounter.get());
                        
                        emitter.complete();
                        log.info("스트림 정상 완료 [{}]", connectionId);
                    } catch (Exception e) {
                        log.error("스트림 완료 처리 오류 [{}]: {}", connectionId, e.getMessage());
                        sendErrorToClient(emitter, connectionId, "완료 처리 실패", e);
                    }
                }
            );
    }
    
    // OpenAI SSE 청크 파싱
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
    
    // 스트림 시작 이벤트 전송
    private void sendStreamStartEvent(SseEmitter emitter, String connectionId, ChatRoomMessageRequest request) {
        try {
            // 히스토리 정보 확인
            boolean isNewChatRoom = request.getChatRoomId() == null;
            int historyCount = 0;
            if (!isNewChatRoom) {
                try {
                    List<Map<String, Object>> messageHistory = buildMessageHistoryForOpenAI(request.getChatRoomId());
                    historyCount = messageHistory.size();
                } catch (Exception e) {
                    log.warn("히스토리 확인 실패 [{}]: {}", connectionId, e.getMessage());
                }
            }
            
            Map<String, Object> requestInfo = Map.of(
                "messageLength", request.getMessage().length(),
                "hasImage", request.getImage() != null,
                "userMessage", request.getMessage(),
                "isNewChatRoom", isNewChatRoom,
                "historyMessageCount", historyCount
            );
            
            Map<String, Object> startEvent = Map.of(
                "eventType", "stream_start",
                "connectionId", connectionId,
                "requestInfo", requestInfo,
                "timestamp", System.currentTimeMillis(),
                "status", "initializing"
            );
            
            emitter.send(SseEmitter.event()
                .name("stream_start")
                .data(startEvent));
                
            log.debug("스트림 시작 이벤트 전송 완료 [{}] - 새 채팅방: {}, 히스토리: {}개", 
                     connectionId, isNewChatRoom, historyCount);
        } catch (Exception e) {
            log.error("스트림 시작 이벤트 전송 실패 [{}]: {}", connectionId, e.getMessage());
        }
    }
    
    // 메시지 청크 이벤트 전송
    private void sendMessageChunkEvent(SseEmitter emitter, String connectionId, String chunkContent, 
                                     String accumulatedMessage, long chunkIndex) {
        try {
            Map<String, Object> chunk = Map.of(
                "content", chunkContent,
                "index", chunkIndex
            );
            
            Map<String, Object> message = Map.of(
                "accumulated", accumulatedMessage,
                "characterCount", accumulatedMessage.length(),
                "chunkCount", chunkIndex
            );
            
            Map<String, Object> metadata = Map.of(
                "connectionId", connectionId
            );
            
            Map<String, Object> chunkEvent = Map.of(
                "eventType", "message_chunk",
                "chunk", chunk,
                "message", message,
                "metadata", metadata,
                "timestamp", System.currentTimeMillis(),
                "status", "streaming"
            );
            
            emitter.send(SseEmitter.event()
                .name("message_chunk")
                .data(chunkEvent));
                
        } catch (Exception e) {
            log.error("메시지 청크 이벤트 전송 실패 [{}]: {}", connectionId, e.getMessage());
            throw new RuntimeException("메시지 청크 이벤트 전송 실패", e);
        }
    }
    
    // 스트림 완료 이벤트 전송
    private void sendStreamCompleteEvent(SseEmitter emitter, String connectionId, String finalMessage, long totalChunks) {
        try {
            String[] words = finalMessage.trim().split("\\s+");
            int wordCount = finalMessage.trim().isEmpty() ? 0 : words.length;
            
            Map<String, Object> finalMessageInfo = Map.of(
                "content", finalMessage,
                "characterCount", finalMessage.length(),
                "wordCount", wordCount,
                "totalChunks", totalChunks
            );
            
            Map<String, Object> metadata = Map.of(
                "connectionId", connectionId,
                "completedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );
            
            Map<String, Object> completeEvent = Map.of(
                "eventType", "stream_complete",
                "finalMessage", finalMessageInfo,
                "metadata", metadata,
                "timestamp", System.currentTimeMillis(),
                "status", "completed"
            );
            
            emitter.send(SseEmitter.event()
                .name("stream_complete")
                .data(completeEvent));
                
            log.debug("스트림 완료 이벤트 전송 완료 [{}]", connectionId);
        } catch (Exception e) {
            log.error("스트림 완료 이벤트 전송 실패 [{}]: {}", connectionId, e.getMessage());
            throw new RuntimeException("스트림 완료 이벤트 전송 실패", e);
        }
    }
    
    // 클라이언트에 에러 정보 전송
    private void sendErrorToClient(SseEmitter emitter, String connectionId, String errorType, Throwable error) {
        try {
            Map<String, Object> errorInfo = Map.of(
                "type", errorType,
                "message", error.getMessage() != null ? error.getMessage() : "알 수 없는 오류",
                "retryable", isRetryableError(error)
            );
            
            Map<String, Object> context = Map.of(
                "connectionId", connectionId
            );
            
            Map<String, Object> errorEvent = Map.of(
                "eventType", "stream_error",
                "error", errorInfo,
                "context", context,
                "timestamp", System.currentTimeMillis(),
                "status", "error"
            );
            
            emitter.send(SseEmitter.event()
                .name("stream_error")
                .data(errorEvent));
            
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
    
    // 재시도 가능한 에러인지 판단
    private boolean isRetryableError(Throwable error) {
        return error instanceof java.util.concurrent.TimeoutException ||
                error instanceof java.net.ConnectException ||
                error instanceof java.io.IOException;
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
    
    // OpenAI API용 메시지 히스토리 구성 (최대 20개 메시지)
    private List<Map<String, Object>> buildMessageHistoryForOpenAI(Long chatRoomId) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
            
            HashMap<String, Object> messages = chatRoom.getMessages();
            
            // 메시지 ID로 정렬 (msg_001, msg_002, ...)
            List<String> sortedMessageIds = messages.keySet().stream()
                    .filter(key -> key.startsWith("msg_"))
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
            
            // 최근 20개 메시지만 선택 (현재 사용자 메시지 제외)
            int maxMessages = 20;
            int startIndex = Math.max(0, sortedMessageIds.size() - maxMessages);
            List<String> recentMessageIds = sortedMessageIds.subList(startIndex, sortedMessageIds.size());
            
            List<Map<String, Object>> messageHistory = new ArrayList<>();
            
            for (String messageId : recentMessageIds) {
                HashMap<String, Object> messageData = TypeConverter.castToHashMap(messages.get(messageId));
                
                String type = (String) messageData.get("type");
                String content = (String) messageData.get("content");
                
                Map<String, Object> openAIMessage = new HashMap<>();
                openAIMessage.put("role", "user".equals(type) ? "user" : "assistant");
                openAIMessage.put("content", content);
                
                messageHistory.add(openAIMessage);
            }
            
            log.debug("채팅방 {} 히스토리 구성 완료: {} 개 메시지", chatRoomId, messageHistory.size());
            return messageHistory;
            
        } catch (BusinessException e) {
            log.error("채팅방 조회 실패 - chatRoomId: {}, error: {}", chatRoomId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("메시지 히스토리 구성 중 오류 발생 - chatRoomId: {}, error: {}", chatRoomId, e.getMessage());
            return new ArrayList<>(); // 빈 히스토리 반환
        }
    }
}
