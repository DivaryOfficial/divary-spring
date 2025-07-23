package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.request.ChatRoomTitleUpdateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomDetailResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("chatrooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

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