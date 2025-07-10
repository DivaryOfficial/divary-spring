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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;  

    // 채팅방 생성 (첫 메시지로)
    @Transactional // TODO : 추후 메서드 분리 예정
    public ChatRoomCreateResponse createChatRoom(ChatRoomCreateRequest request) {
        // 임시로 사용자 ID 하드코딩
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        Long userId = 1L;
        
        String title = openAIService.generateTitle(request.getFirstMessage());
        
        // 첫 메시지 저장
        HashMap<String, Object> initialMessages = new HashMap<>();
        HashMap<String, Object> firstMessageData = new HashMap<>();
        firstMessageData.put("content", request.getFirstMessage());
        firstMessageData.put("timestamp", System.currentTimeMillis());
        firstMessageData.put("type", "user");
        
        // 이미지 처리
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            firstMessageData.put("hasImage", true);
            firstMessageData.put("imageName", request.getImage().getOriginalFilename());
            firstMessageData.put("imageSize", request.getImage().getSize());
        }
        
        // OpenAI API 호출 (이미지 파일 직접 전달)
        OpenAIResponse aiResponse = openAIService.sendMessage(request.getFirstMessage(), request.getImage());
        
        // AI 응답 메시지 저장
        HashMap<String, Object> assistantMessageData = new HashMap<>();
        assistantMessageData.put("content", aiResponse.getContent());
        assistantMessageData.put("timestamp", System.currentTimeMillis());
        assistantMessageData.put("type", "assistant");
        
        // 첫 메시지와 AI 응답 메시지는 순번이 정해져 있음. 후에 대화 메세지 추가 시 순번 자동 증가
        initialMessages.put("msg_001", firstMessageData); 
        initialMessages.put("msg_002", assistantMessageData);
        
        // 메타데이터 설정
        ChatRoomMetadata.Usage usage = ChatRoomMetadata.Usage.builder()
                .promptTokens(aiResponse.getPromptTokens())
                .completionTokens(aiResponse.getCompletionTokens())
                .totalTokens(aiResponse.getTotalTokens())
                .model(aiResponse.getModel())
                .cost(aiResponse.getCost())
                .build();
        
        ChatRoomMetadata metadata = ChatRoomMetadata.builder()
                .lastMessageId("msg_002")
                .messageCount(2)
                .usage(usage)
                .build();
        
        HashMap<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("lastMessageId", metadata.getLastMessageId());
        metadataMap.put("messageCount", metadata.getMessageCount());
        metadataMap.put("usage", metadata.getUsage());
        
        ChatRoom chatRoom = ChatRoom.builder()
                .userId(userId)
                .title(title)
                .messages(initialMessages)
                .metadata(metadataMap)
                .build();

        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        // 응답 DTO 생성
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.from(savedChatRoom);
        
        // 사용자 메시지 DTO 생성
        Message userMessage = Message.builder()
                .id("msg_001")
                .role("user")
                .content(request.getFirstMessage())
                .timestamp(LocalDateTime.now())
                .build();
        
        // 이미지 첨부파일이 있는 경우 추가
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            Message.AttachmentDto attachment = Message.AttachmentDto.builder()
                    .id(1L)
                    .fileUrl("https://example_url_image.com") // TODO: s3 이미지 업로드 후 이미지 URL 설정
                    .originalFilename(request.getImage().getOriginalFilename())
                    .build();
            userMessage.setAttachments(List.of(attachment));
        }
        
        // AI 어시스턴트 메시지 DTO 생성
        Message assistantMessage = Message.builder()
                .id("msg_002")
                .role("assistant")
                .content(aiResponse.getContent())
                .timestamp(LocalDateTime.now().plusSeconds(15))
                .build();
        
        return ChatRoomCreateResponse.builder()
                .chatRoom(chatRoomResponse)
                .userMessage(userMessage)
                .assistantMessage(assistantMessage)
                .usage(usage)
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