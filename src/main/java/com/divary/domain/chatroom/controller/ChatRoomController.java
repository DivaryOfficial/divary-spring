package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.request.ChatRoomTitleUpdateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomDetailResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("chatrooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "채팅방 메시지 전송", description = "새 채팅방 생성 또는 기존 채팅방에 메시지 전송\n chatRoomId 없으면 새 채팅방 생성\n 보낸 메시지와 AI 응답만 반환")
    @ApiSuccessResponse(dataType = ChatRoomMessageResponse.class)
    public ApiResponse<ChatRoomMessageResponse> sendChatRoomMessage(
            @Valid @ModelAttribute ChatRoomMessageRequest request) {
        
        ChatRoomMessageResponse response = chatRoomService.sendChatRoomMessage(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 상세 조회", description = "채팅방의 상세 정보를 조회합니다.")
    @ApiSuccessResponse(dataType = ChatRoomDetailResponse.class)
    public ApiResponse<ChatRoomDetailResponse> getChatRoomDetail(@PathVariable Long chatRoomId) {
        ChatRoomDetailResponse response = chatRoomService.getChatRoomDetail(chatRoomId);
        return ApiResponse.success(response);
    }


    @GetMapping
    @Operation(summary = "채팅방 목록 조회", description = "사용자의 채팅방 목록을 조회합니다.")
    public ApiResponse<List<ChatRoomResponse>> getChatRooms() {
        // 임시로 사용자 ID 하드코딩
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        Long userId = 1L;
        
        List<ChatRoomResponse> responses = chatRoomService.getChatRoomsByUserId(userId);
        return ApiResponse.success(responses);
    }

    @DeleteMapping("/{chatRoomId}")
    @Operation(summary = "채팅방 삭제", description = "채팅방을 삭제합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    public ApiResponse<Void> deleteChatRoom(@PathVariable Long chatRoomId) {
        // 임시로 사용자 ID 하드코딩
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        Long userId = 1L;
        
        chatRoomService.deleteChatRoom(chatRoomId, userId);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{chatRoomId}/title")
    @Operation(summary = "채팅방 제목 변경", description = "채팅방의 제목을 변경합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    public ApiResponse<Void> updateChatRoomTitle(@PathVariable Long chatRoomId,
                                                @Valid @RequestBody ChatRoomTitleUpdateRequest request) {
        // 임시로 사용자 ID 하드코딩
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        Long userId = 1L;
        
        chatRoomService.updateChatRoomTitle(chatRoomId, userId, request.getTitle());
        return ApiResponse.success(null);
    }
} 