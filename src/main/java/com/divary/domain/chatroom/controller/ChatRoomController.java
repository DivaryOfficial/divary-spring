package com.divary.domain.chatroom.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomCreateResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.service.ChatRoomService;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ImageService imageService;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "채팅방 생성", description = "첫 메시지로 새로운 채팅방을 생성합니다.")
    @ApiSuccessResponse(dataType = ChatRoomCreateResponse.class)
    public ApiResponse<ChatRoomCreateResponse> createChatRoom(
            @Valid @ModelAttribute ChatRoomCreateRequest request) {
        
        // 이미지가 있는 경우 S3에 업로드
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // TODO: 채팅방 ID를 동적으로 설정 (현재는 임시로 "temp" 사용)
            ImageResponse imageResponse = imageService.uploadImageByType(
                    ImageType.USER_CHAT, 
                    request.getImage(), 
                    1L, // TODO: 실제 사용자 ID로 변경
                    "temp"
            );
            imageUrl = imageResponse.getFileUrl();
        }
        
        ChatRoomCreateResponse response = chatRoomService.createChatRoom(request, imageUrl);
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