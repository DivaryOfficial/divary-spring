package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.request.ChatRoomTitleUpdateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomDetailResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.domain.chatroom.service.ChatRoomStreamService;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("chatrooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatRoomStreamService chatRoomStreamService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "채팅방 메시지 전송", description = "새 채팅방 생성 또는 기존 채팅방에 메시지 전송\n chatRoomId 없으면 새 채팅방 생성\n 보낸 메시지와 AI 응답만 반환")
    @ApiSuccessResponse(dataType = ChatRoomMessageResponse.class)
    @ApiErrorExamples(value = {ErrorCode.CHAT_ROOM_ACCESS_DENIED, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<ChatRoomMessageResponse> sendChatRoomMessage(
            @Valid @ModelAttribute ChatRoomMessageRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        ChatRoomMessageResponse response = chatRoomService.sendChatRoomMessage(request, userPrincipal.getId());
        return ApiResponse.success(response);
    }

    @PostMapping(value = "/stream", consumes = "multipart/form-data", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "채팅방 메시지 스트리밍 전송 (SSE)", 
            description = """        
            **실시간 스트리밍 채팅 API (Server-Sent Events)**
            - OpenAI GPT-5-nano 모델 기반 스트리밍 응답
            - 메시지 청크 단위로 실시간 전송
            - 이미지 업로드 지원 (multipart/form-data)
            - chatRoomId 없으면 새 채팅방 생성, 있으면 기존 채팅방 사용
            
            **이벤트 타입:**
            - stream_start: 스트림 시작 정보
            - message_chunk: 실시간 메시지 청크
            - stream_complete: 스트림 완료 및 통계
            - stream_error: 오류 발생 시
            
            **iOS 호환:**
            - EventSource API 지원
            - 자동 재연결 지원
            """
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "text/event-stream (SSE)",
        content = @Content(
            mediaType = MediaType.TEXT_EVENT_STREAM_VALUE,
            examples = {
                @ExampleObject(
                    name = "stream_start",
                    value = "event: stream_start\ndata:{\"timestamp\":1756451904719,\"requestInfo\":{\"hasImage\":false,\"messageLength\":34},\"eventType\":\"stream_start\",\"connectionId\":\"conn_1_1\"}\n\n"
                ),
                @ExampleObject(
                    name = "message_chunk",
                    value = "event: message_chunk\ndata:{\"message\":{\"characterCount\":2,\"accumulated\":\"가장\"},\"timestamp\":1756451906306,\"chunk\":{\"index\":2,\"content\":\"장\"},\"eventType\":\"message_chunk\"}\n\n"
                ),
                @ExampleObject(
                    name = "stream_complete",
                    value = "event: stream_complete\ndata:{\"finalMessage\":{\"wordCount\":85,\"totalChunks\":235,\"content\":\"...\"},\"timestamp\":1756451907714,\"eventType\":\"stream_complete\"}\n\n"
                ),
                @ExampleObject(
                    name = "stream_error",
                    value = "event: stream_error\ndata:{\"eventType\":\"stream_error\",\"error\":{\"type\":\"스트림 처리 오류(OpenAI 응답 지연)\",\"message\":\"응답 지연으로 스트림이 중단되었습니다.\"},\"timestamp\":1756371342000}\n\n"
                )
            }
        )
    )
    @ApiSuccessResponse(dataType = SseEmitter.class)
    @ApiErrorExamples(value = {ErrorCode.CHAT_ROOM_ACCESS_DENIED, ErrorCode.AUTHENTICATION_REQUIRED, ErrorCode.INTERNAL_SERVER_ERROR})
    public SseEmitter streamChatRoomMessage(
            @Valid @ModelAttribute ChatRoomMessageRequest request,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        log.info("스트리밍 채팅 요청 수신 - userId: {}, chatRoomId: {}, messageLength: {}", 
                userPrincipal.getId(), request.getChatRoomId(), 
                request.getMessage() != null ? request.getMessage().length() : 0);

        return chatRoomStreamService.streamChatRoomMessage(request, userPrincipal.getId());
    }

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 상세 조회", description = "채팅방의 상세 정보를 조회합니다.")
    @ApiSuccessResponse(dataType = ChatRoomDetailResponse.class)
    @ApiErrorExamples(value = {ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ROOM_ACCESS_DENIED, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<ChatRoomDetailResponse> getChatRoomDetail(@PathVariable Long chatRoomId) {
        ChatRoomDetailResponse response = chatRoomService.getChatRoomDetail(chatRoomId);
        return ApiResponse.success(response);
    }


    @GetMapping
    @Operation(summary = "채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    @ApiSuccessResponse(dataType = ChatRoomResponse.class, isArray = true)
    @ApiErrorExamples(value = {ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<List<ChatRoomResponse>> getChatRooms(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        
        Long userId = userPrincipal.getId();
        
        List<ChatRoomResponse> responses = chatRoomService.getChatRoomsByUserId(userId);
        return ApiResponse.success(responses);
    }

    @DeleteMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 삭제", description = "채팅방을 삭제합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    @ApiErrorExamples(value = {ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ROOM_ACCESS_DENIED, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<Void> deleteChatRoom(@PathVariable Long chatRoomId,
                                        @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        
        chatRoomService.deleteChatRoom(chatRoomId, userId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{chatRoomId}/title")
    @Operation(summary = "채팅방 제목 변경", description = "채팅방의 제목을 변경합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    @ApiErrorExamples(value = {ErrorCode.CHAT_ROOM_NOT_FOUND, ErrorCode.CHAT_ROOM_ACCESS_DENIED, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<Void> updateChatRoomTitle(@PathVariable Long chatRoomId,
                                                @Valid @RequestBody ChatRoomTitleUpdateRequest request,
                                                @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        
        chatRoomService.updateChatRoomTitle(chatRoomId, userId, request.getTitle());
        return ApiResponse.success(null);
    }
} 