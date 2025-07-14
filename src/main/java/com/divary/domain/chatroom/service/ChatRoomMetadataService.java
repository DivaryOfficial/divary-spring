package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.ChatRoomMetadata;
import com.divary.domain.chatroom.dto.response.OpenAIResponse;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class ChatRoomMetadataService {

    // 메타데이터 생성
    public ChatRoomMetadata createMetadata(OpenAIResponse aiResponse, String lastMessageId, int messageCount) {
        ChatRoomMetadata.Usage usage = ChatRoomMetadata.Usage.builder()
                .promptTokens(aiResponse.getPromptTokens())
                .completionTokens(aiResponse.getCompletionTokens())
                .totalTokens(aiResponse.getTotalTokens())
                .model(aiResponse.getModel())
                .cost(aiResponse.getCost())
                .build();
        
        return ChatRoomMetadata.builder()
                .lastMessageId(lastMessageId)
                .messageCount(messageCount)
                .usage(usage)
                .build();
    }

    // 메타데이터를 HashMap으로 변환
    public HashMap<String, Object> convertToMap(ChatRoomMetadata metadata) {
        HashMap<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("lastMessageId", metadata.getLastMessageId());
        metadataMap.put("messageCount", metadata.getMessageCount());
        metadataMap.put("usage", metadata.getUsage());
        
        return metadataMap;
    }
}