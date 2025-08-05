package com.divary.domain.chatroom.service;

import com.divary.domain.chatroom.dto.request.ChatRoomMessageRequest;
import com.divary.domain.chatroom.repository.ChatRoomRepository;
import com.divary.domain.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRoomStreamService {

    private final ChatRoomRepository chatRoomRepository;
    private final OpenAIService openAIService;
    private final MessageFactory messageFactory;
    private final ImageService imageService;

    public SseEmitter streamChatRoomMessage(ChatRoomMessageRequest request, Long userId) {
        // SseEmitter를 생성하고 즉시 반환합니다.
        // 실제 로직은 다음 단계에서 이 메서드 내부에 구현될 것입니다.
        SseEmitter emitter = new SseEmitter(60_000L); // 1분 타임아웃
        return emitter;
    }
}
