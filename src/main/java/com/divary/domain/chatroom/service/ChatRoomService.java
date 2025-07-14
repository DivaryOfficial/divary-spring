package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.ChatRoomMetadata;
import com.divary.domain.chatroom.dto.Message;
import com.divary.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomCreateResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.dto.response.OpenAIResponse;
import com.divary.domain.chatroom.entity.ChatRoom;
import com.divary.domain.chatroom.repository.ChatRoomRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;
    private final MessageFactory messageFactory;
    private final ChatRoomMetadataService metadataService;  

    // 채팅방 생성 (첫 메시지로)
    @Transactional
    public ChatRoomCreateResponse createChatRoom(ChatRoomCreateRequest request, String imageUrl) {
        Long userId = getCurrentUserId();
        String title = generateChatRoomTitle(request.getFirstMessage());
        OpenAIResponse aiResponse = processMessageWithAI(request.getFirstMessage(), request.getImage());
        
        ChatRoom chatRoom = buildChatRoom(userId, title, request, aiResponse);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        return buildCreateResponse(savedChatRoom, request, aiResponse, imageUrl);
    }

    // 현재 사용자 ID 가져오기
    private Long getCurrentUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    // 채팅방 제목 생성
    private String generateChatRoomTitle(String firstMessage) {
        return openAIService.generateTitle(firstMessage);
    }

    // AI로 메시지 처리
    private OpenAIResponse processMessageWithAI(String message, org.springframework.web.multipart.MultipartFile image) {
        return openAIService.sendMessage(message, image);
    }

    // ChatRoom 엔티티 생성
    private ChatRoom buildChatRoom(Long userId, String title, ChatRoomCreateRequest request, OpenAIResponse aiResponse) {
        HashMap<String, Object> initialMessages = messageFactory.createInitialMessages(
                request.getFirstMessage(), request.getImage(), aiResponse);
        
        ChatRoomMetadata metadata = metadataService.createMetadata(aiResponse);
        HashMap<String, Object> metadataMap = metadataService.convertToMap(metadata);
        
        return ChatRoom.builder()
                .userId(userId)
                .title(title)
                .messages(initialMessages)
                .metadata(metadataMap)
                .build();
    }

    // 응답 DTO 생성
    private ChatRoomCreateResponse buildCreateResponse(ChatRoom savedChatRoom, ChatRoomCreateRequest request, OpenAIResponse aiResponse, String imageUrl) {
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.from(savedChatRoom);
        
        // 이미지가 있는 경우와 없는 경우를 구분하여 메시지 생성
        Message userMessage;
        if (request.getImage() != null && !request.getImage().isEmpty() && imageUrl != null) {
            userMessage = messageFactory.createUserMessageDtoWithAttachment(
                    "msg_001", 
                    request.getFirstMessage(), 
                    1L, 
                    imageUrl, 
                    request.getImage().getOriginalFilename()
            );
        } else {
            userMessage = messageFactory.createUserMessageDto("msg_001", request.getFirstMessage());
        }
        
        Message assistantMessage = messageFactory.createAssistantMessageDto("msg_002", aiResponse);
        
        ChatRoomMetadata metadata = metadataService.createMetadata(aiResponse);
        
        return ChatRoomCreateResponse.builder()
                .chatRoom(chatRoomResponse)
                .userMessage(userMessage)
                .assistantMessage(assistantMessage)
                .usage(metadata.getUsage())
                .build();
    }

    // 사용자별 채팅방 목록 조회
    public List<ChatRoomResponse> getChatRoomsByUserId(Long userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByUserIdOrderByCreatedAtDesc(userId);
        
        return chatRooms.stream()
                .map(ChatRoomResponse::from)
                .collect(Collectors.toList());
    }
} 