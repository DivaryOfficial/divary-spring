package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.ChatRoomMetadata;
import com.divary.domain.chatroom.dto.Message;
import com.divary.domain.chatroom.dto.request.ChatRoomCreateRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomCreateResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.dto.response.OpenAIResponse;
import com.divary.domain.chatroom.entity.ChatRoom;
import com.divary.domain.chatroom.repository.ChatRoomRepository;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;

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
    private final ImageService imageService;  

    // 채팅방 생성 (첫 메시지로)
    @Transactional
    public ChatRoomCreateResponse createChatRoom(ChatRoomCreateRequest request) {
        Long userId = getCurrentUserId();
        String title = openAIService.generateTitle(request.getFirstMessage());
        
        // 채팅방을 먼저 저장 (이미지 없이) : chatRoomId를 반환 받기 위해서 먼저 저장, 이미지는 chatRoomId에 매핑되는 구조기에
        ChatRoom chatRoom = buildChatRoomWithoutImage(userId, title, request);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        // 채팅방 ID로 이미지 업로드
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            ImageResponse imageResponse = imageService.uploadImageByType(
                ImageType.USER_CHAT,
                request.getImage(),
                userId,
                savedChatRoom.getId().toString()
            );
            imageUrl = imageResponse.getFileUrl();
            
            // 채팅방에 이미지 정보 업데이트
            updateChatRoomWithImage(savedChatRoom, imageUrl, request.getImage());
        }
        
        // AI 응답 생성
        OpenAIResponse aiResponse = openAIService.sendMessage(request.getFirstMessage(), request.getImage());
        
        // AI 응답을 채팅방에 추가
        addAiResponseToMessages(savedChatRoom, aiResponse);
        
        return buildCreateResponse(savedChatRoom, request, aiResponse, imageUrl);
    }

    // AI 응답을 채팅방 메시지에 추가
    private void addAiResponseToMessages(ChatRoom chatRoom, OpenAIResponse aiResponse) {
        HashMap<String, Object> messages = chatRoom.getMessages();
        HashMap<String, Object> assistantMessage = messageFactory.createAssistantMessageData(aiResponse);
        
        // 다음 메시지 ID 생성
        String nextMessageId = messageFactory.generateNextMessageId(messages);
        messages.put(nextMessageId, assistantMessage);
        
        // 메타데이터 업데이트
        ChatRoomMetadata metadata = metadataService.createMetadata(aiResponse, nextMessageId, messages.size());
        HashMap<String, Object> metadataMap = metadataService.convertToMap(metadata);
        
        chatRoom.updateMessages(messages);
        chatRoom.updateMetadata(metadataMap);
    }
    

    // 현재 사용자 ID 가져오기
    private Long getCurrentUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    // ChatRoom 엔티티 생성 (이미지 없이) 
    private ChatRoom buildChatRoomWithoutImage(Long userId, String title, ChatRoomCreateRequest request) {
        // 이미지 없이 초기 메시지만 생성
        HashMap<String, Object> initialMessages = messageFactory.createUserMessageData(request.getFirstMessage(), null);
        HashMap<String, Object> messages = new HashMap<>();
        String firstMessageId = messageFactory.generateNextMessageId(messages);
        messages.put(firstMessageId, initialMessages);
        
        // 임시 메타데이터 (AI 응답 전)
        HashMap<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("lastMessageId", firstMessageId);
        metadataMap.put("messageCount", 1);
        
        return ChatRoom.builder()
                .userId(userId)
                .title(title)
                .messages(messages)
                .metadata(metadataMap)
                .build();
    }

    // 채팅방에 이미지 정보 업데이트
    private void updateChatRoomWithImage(ChatRoom chatRoom, String imageUrl, org.springframework.web.multipart.MultipartFile image) {
        HashMap<String, Object> messages = chatRoom.getMessages();
        HashMap<String, Object> metadata = chatRoom.getMetadata();
        String firstMessageId = (String) metadata.get("lastMessageId");
        
        @SuppressWarnings("unchecked")
        HashMap<String, Object> userMessage = (HashMap<String, Object>) messages.get(firstMessageId);
        
        // 이미지 정보 추가
        userMessage.put("hasImage", true);
        userMessage.put("imageName", image.getOriginalFilename());
        userMessage.put("imageSize", image.getSize());
        userMessage.put("imageUrl", imageUrl);
        
        messages.put(firstMessageId, userMessage);
        chatRoom.updateMessages(messages);
    }

    // 응답 DTO 생성
    private ChatRoomCreateResponse buildCreateResponse(ChatRoom savedChatRoom, ChatRoomCreateRequest request, OpenAIResponse aiResponse, String imageUrl) {
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.from(savedChatRoom);
        
        // 메타데이터에서 마지막 메시지 ID 가져오기
        HashMap<String, Object> metadata = savedChatRoom.getMetadata();
        String lastMessageId = (String) metadata.get("lastMessageId");
        
        // 이미지가 있는 경우와 없는 경우를 구분하여 메시지 생성
        String firstMessageId = messageFactory.generateNextMessageId(new HashMap<>());
        Message userMessage;
        if (request.getImage() != null && !request.getImage().isEmpty() && imageUrl != null) {
            userMessage = messageFactory.createUserMessageDtoWithAttachment(
                    firstMessageId, 
                    request.getFirstMessage(), 
                    1L, 
                    imageUrl, 
                    request.getImage().getOriginalFilename()
            );
        } else {
            userMessage = messageFactory.createUserMessageDto(firstMessageId, request.getFirstMessage());
        }
        
        Message assistantMessage = messageFactory.createAssistantMessageDto(lastMessageId, aiResponse);
        
        ChatRoomMetadata chatRoomMetadata = metadataService.createMetadata(aiResponse, lastMessageId, metadata.size());
        
        return ChatRoomCreateResponse.builder()
                .chatRoom(chatRoomResponse)
                .userMessage(userMessage)
                .assistantMessage(assistantMessage)
                .usage(chatRoomMetadata.getUsage())
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