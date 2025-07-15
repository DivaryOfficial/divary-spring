package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.ChatRoomMetadata;
import com.divary.domain.chatroom.dto.Message;
import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.dto.response.ChatRoomDetailResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomMessageResponse;
import com.divary.domain.chatroom.dto.response.ChatRoomResponse;
import com.divary.domain.chatroom.dto.response.OpenAIResponse;
import com.divary.domain.chatroom.entity.ChatRoom;
import com.divary.domain.chatroom.repository.ChatRoomRepository;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.common.converter.TypeConverter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;
    private final MessageFactory messageFactory;
    private final ImageService imageService;  

    // 채팅방 메시지 전송 (새 채팅방 생성 또는 기존 채팅방에 메시지 추가)
    @Transactional
    public ChatRoomMessageResponse sendChatRoomMessage(ChatRoomMessageRequest request) {
        Long userId = getCurrentUserId();
        ChatRoom chatRoom;
        List<String> newMessageIds = new java.util.ArrayList<>();
        
        if (request.getChatRoomId() == null) {
            // 새 채팅방 생성
            chatRoom = createNewChatRoom(userId, request);
            // 새 채팅방의 경우 첫 번째 메시지 ID를 추가
            HashMap<String, Object> metadata = chatRoom.getMetadata();
            newMessageIds.add((String) metadata.get("lastMessageId"));
        } else {
            // 기존 채팅방에 메시지 추가
            chatRoom = addMessageToExistingChatRoom(request.getChatRoomId(), userId, request);
            // 방금 추가한 사용자 메시지 ID를 추가
            HashMap<String, Object> metadata = chatRoom.getMetadata();
            newMessageIds.add((String) metadata.get("lastMessageId"));
        }
        
        // AI 응답 생성
        OpenAIResponse aiResponse;
        if (request.getChatRoomId() == null) {
            // 새 채팅방 - 히스토리 없음
            aiResponse = openAIService.sendMessage(request.getMessage(), request.getImage());
        } else {
            // 기존 채팅방 - 히스토리 포함
            List<Map<String, Object>> messageHistory = buildMessageHistoryForOpenAI(chatRoom);
            aiResponse = openAIService.sendMessageWithHistory(request.getMessage(), request.getImage(), messageHistory);
        }
        
        // AI 응답을 채팅방에 추가
        String aiMessageId = addAiResponseToMessages(chatRoom, aiResponse);
        newMessageIds.add(aiMessageId);
        
        return buildMessageResponse(chatRoom, newMessageIds);
    }

    // AI 응답을 채팅방 메시지에 추가
    private String addAiResponseToMessages(ChatRoom chatRoom, OpenAIResponse aiResponse) {
        HashMap<String, Object> messages = chatRoom.getMessages();
        HashMap<String, Object> assistantMessage = messageFactory.createAssistantMessageData(aiResponse);
        
        // 다음 메시지 ID 생성
        String nextMessageId = messageFactory.generateNextMessageId(messages);
        messages.put(nextMessageId, assistantMessage);
        
        // 기존 메타데이터 가져와서 업데이트
        HashMap<String, Object> metadata = chatRoom.getMetadata();
        metadata.put("lastMessageId", nextMessageId);
        metadata.put("messageCount", messages.size());
        
        // Usage 정보 업데이트
        ChatRoomMetadata.Usage usage = ChatRoomMetadata.Usage.builder()
                .promptTokens(aiResponse.getPromptTokens())
                .completionTokens(aiResponse.getCompletionTokens())
                .totalTokens(aiResponse.getTotalTokens())
                .model(aiResponse.getModel())
                .cost(aiResponse.getCost())
                .build();
        metadata.put("usage", usage);
        
        chatRoom.updateMessages(messages);
        chatRoom.updateMetadata(metadata);
        
        return nextMessageId;
    }
    

    // 현재 사용자 ID 가져오기
    private Long getCurrentUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    // ChatRoom 엔티티 생성 (이미지 없이) 
    private ChatRoom buildChatRoomWithoutImage(Long userId, String title, ChatRoomMessageRequest request) {
        // 이미지 없이 초기 메시지만 생성
        HashMap<String, Object> initialMessages = messageFactory.createUserMessageData(request.getMessage(), null);
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
        HashMap<String, Object> userMessage = TypeConverter.castToHashMap(messages.get(firstMessageId));
        
        // 이미지 정보 추가
        userMessage.put("hasImage", true);
        userMessage.put("imageName", image.getOriginalFilename());
        userMessage.put("imageUrl", imageUrl);
        
        messages.put(firstMessageId, userMessage);
        chatRoom.updateMessages(messages);
    }

    // 응답 DTO 생성
    private ChatRoomDetailResponse buildDetailResponse(ChatRoom savedChatRoom) {
        ChatRoomResponse chatRoomResponse = ChatRoomResponse.from(savedChatRoom);
        
        // 전체 메시지 변환
        List<Message> messages = messageFactory.convertToMessageList(savedChatRoom.getMessages());
        
        // 메타데이터에서 사용량 정보 가져오기
        HashMap<String, Object> metadata = savedChatRoom.getMetadata();
        ChatRoomMetadata.Usage usage = null;
        Object usageObj = metadata.get("usage");
        if (usageObj instanceof ChatRoomMetadata.Usage) {
            usage = (ChatRoomMetadata.Usage) usageObj;
        } else if (usageObj instanceof HashMap) {
            HashMap<String, Object> usageMap = TypeConverter.castToHashMap(usageObj);
            usage = ChatRoomMetadata.Usage.builder()
                    .promptTokens((Integer) usageMap.get("promptTokens"))
                    .completionTokens((Integer) usageMap.get("completionTokens"))
                    .totalTokens((Integer) usageMap.get("totalTokens"))
                    .model((String) usageMap.get("model"))
                    .cost((Double) usageMap.get("cost"))
                    .build();
        }
        
        return ChatRoomDetailResponse.builder()
                .chatRoom(chatRoomResponse)
                .messages(messages)
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
    
    // 새 채팅방 생성
    private ChatRoom createNewChatRoom(Long userId, ChatRoomMessageRequest request) {
        String title = openAIService.generateTitle(request.getMessage());
        
        // 채팅방을 먼저 저장 (이미지 없이)
        ChatRoom chatRoom = buildChatRoomWithoutImage(userId, title, request);
        ChatRoom savedChatRoom = chatRoomRepository.save(chatRoom);
        
        // 이미지가 있으면 업로드 후 메시지에 추가
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            ImageResponse imageResponse = imageService.uploadImageByType(
                ImageType.USER_CHAT,
                request.getImage(),
                userId,
                savedChatRoom.getId().toString()
            );
            String imageUrl = imageResponse.getFileUrl();
            updateChatRoomWithImage(savedChatRoom, imageUrl, request.getImage());
        }
        
        return savedChatRoom;
    }
    
    // 기존 채팅방에 메시지 추가
    private ChatRoom addMessageToExistingChatRoom(Long chatRoomId, Long userId, ChatRoomMessageRequest request) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // TODO: 채팅방 소유자 확인 로직 - 현재는 하드코딩으로 처리
        // if (!chatRoom.getUserId().equals(userId)) {
        //     throw new BusinessException(ErrorCode.ACCESS_DENIED);
        // }
        
        // 새 메시지 추가
        addUserMessageToChatRoom(chatRoom, request);
        
        return chatRoom;
    }
    
    // 사용자 메시지를 채팅방에 추가
    private void addUserMessageToChatRoom(ChatRoom chatRoom, ChatRoomMessageRequest request) {
        HashMap<String, Object> messages = chatRoom.getMessages();
        String newMessageId = messageFactory.generateNextMessageId(messages);
        
        // 메시지 데이터 생성
        HashMap<String, Object> messageData = messageFactory.createUserMessageData(request.getMessage(), null);
        
        // 이미지가 있으면 업로드
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            ImageResponse imageResponse = imageService.uploadImageByType(
                ImageType.USER_CHAT,
                request.getImage(),
                getCurrentUserId(),
                chatRoom.getId().toString()
            );
            String imageUrl = imageResponse.getFileUrl();
            
            // 이미지 정보 추가
            messageData.put("hasImage", true);
            messageData.put("imageName", request.getImage().getOriginalFilename());
            messageData.put("imageUrl", imageUrl);
        }
        
        messages.put(newMessageId, messageData);
        
        // 메타데이터 업데이트
        HashMap<String, Object> metadata = chatRoom.getMetadata();
        metadata.put("lastMessageId", newMessageId);
        metadata.put("messageCount", messages.size());
        
        chatRoom.updateMessages(messages);
        chatRoom.updateMetadata(metadata);
    }
    
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));

        return buildDetailResponse(chatRoom);
    }
    
    // OpenAI API용 메시지 히스토리 구성 (최대 20개 메시지)
    private List<Map<String, Object>> buildMessageHistoryForOpenAI(ChatRoom chatRoom) {
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
        
        List<Map<String, Object>> messageHistory = new java.util.ArrayList<>();
        
        for (String messageId : recentMessageIds) {
            HashMap<String, Object> messageData = TypeConverter.castToHashMap(messages.get(messageId));
            
            String type = (String) messageData.get("type");
            String content = (String) messageData.get("content");
            
            Map<String, Object> openAIMessage = new HashMap<>();
            openAIMessage.put("role", "user".equals(type) ? "user" : "assistant");
            openAIMessage.put("content", content);
            
            messageHistory.add(openAIMessage);
        }
        
        return messageHistory;
    }
    
    // 새 메시지만 포함한 응답 생성
    private ChatRoomMessageResponse buildMessageResponse(ChatRoom chatRoom, List<String> newMessageIds) {
        // 새 메시지들만 변환
        HashMap<String, Object> allMessages = chatRoom.getMessages();
        HashMap<String, Object> newMessagesMap = new HashMap<>();
        
        for (String messageId : newMessageIds) {
            newMessagesMap.put(messageId, allMessages.get(messageId));
        }
        
        List<Message> newMessages = messageFactory.convertToMessageList(newMessagesMap);
        
        // 메타데이터에서 사용량 정보 가져오기
        HashMap<String, Object> metadata = chatRoom.getMetadata();
        ChatRoomMetadata.Usage usage = null;
        Object usageObj = metadata.get("usage");
        if (usageObj instanceof ChatRoomMetadata.Usage) {
            usage = (ChatRoomMetadata.Usage) usageObj;
        } else if (usageObj instanceof HashMap) {
            HashMap<String, Object> usageMap = TypeConverter.castToHashMap(usageObj);
            usage = ChatRoomMetadata.Usage.builder()
                    .promptTokens((Integer) usageMap.get("promptTokens"))
                    .completionTokens((Integer) usageMap.get("completionTokens"))
                    .totalTokens((Integer) usageMap.get("totalTokens"))
                    .model((String) usageMap.get("model"))
                    .cost((Double) usageMap.get("cost"))
                    .build();
        }
        
        return ChatRoomMessageResponse.builder()
                .chatRoomId(chatRoom.getId())
                .title(chatRoom.getTitle())
                .newMessages(newMessages)
                .usage(usage)
                .build();
    }
    
    // 채팅방 삭제
    @Transactional
    public void deleteChatRoom(Long chatRoomId, Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 채팅방 소유자 확인
        // TODO: 채팅방 소유자 확인 로직 - 현재는 하드코딩으로 처리
        if (!chatRoom.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        
        chatRoomRepository.delete(chatRoom);
    }
    
    // 채팅방 제목 변경
    @Transactional
    public void updateChatRoomTitle(Long chatRoomId, Long userId, String title) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        
        // 채팅방 소유자 확인
        // TODO: 채팅방 소유자 확인 로직 - 현재는 하드코딩으로 처리
        if (!chatRoom.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        
        chatRoom.updateTitle(title);
    }
} 