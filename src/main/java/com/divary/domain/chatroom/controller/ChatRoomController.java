package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomDetailResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    @Operation(summary = "새 채팅방 생성", description = "첫 메시지로 새로운 채팅방을 생성합니다.")
    @ApiSuccessResponse(dataType = ChatRoomDetailResponse.class)
    public ApiResponse<ChatRoomDetailResponse> createChatRoom(
            @RequestParam("message") @NotBlank String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        ChatRoomMessageRequest request = new ChatRoomMessageRequest();
        request.setChatRoomId(null); // 새 채팅방
        request.setMessage(message);
        request.setImage(image);
        
        ChatRoomDetailResponse response = chatRoomService.sendChatRoomMessage(request);
        return ApiResponse.success(response);
    }

    @PostMapping(value = "/{chatRoomId}/messages", consumes = "multipart/form-data")
    @Operation(summary = "메시지 전송", description = "기존 채팅방에 새 메시지를 전송합니다.")
    @ApiSuccessResponse(dataType = ChatRoomDetailResponse.class)
    public ApiResponse<ChatRoomDetailResponse> sendMessage(
            @PathVariable Long chatRoomId,
            @RequestParam("message") @NotBlank String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        ChatRoomMessageRequest request = new ChatRoomMessageRequest();
        request.setChatRoomId(chatRoomId);
        request.setMessage(message);
        request.setImage(image);
        
        ChatRoomDetailResponse response = chatRoomService.sendChatRoomMessage(request);
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
} 