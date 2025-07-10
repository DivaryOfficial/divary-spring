package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomCreateResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "채팅방 생성", description = "첫 메시지로 새로운 채팅방을 생성합니다.")
    @ApiSuccessResponse(dataType = ChatRoomCreateResponse.class)
    public ApiResponse<ChatRoomCreateResponse> createChatRoom(
            @Valid @ModelAttribute ChatRoomCreateRequest request) {
        
        ChatRoomCreateResponse response = chatRoomService.createChatRoom(request);
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
} 