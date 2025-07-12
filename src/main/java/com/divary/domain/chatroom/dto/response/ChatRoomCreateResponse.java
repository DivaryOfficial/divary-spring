package com.divary.domain.chatroom.dto.response;

import com.divary.domain.chatroom.dto.ChatRoomMetadata;
import com.divary.domain.chatroom.dto.Message;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 생성 응답")
public class ChatRoomCreateResponse {
    
    @Schema(description = "채팅방 정보")
    private ChatRoomResponse chatRoom;
    
    @Schema(description = "사용자 메시지")
    private Message userMessage;
    
    @Schema(description = "AI 어시스턴트 메시지")
    private Message assistantMessage;
    
    @Schema(description = "API 사용량 정보")
    private ChatRoomMetadata.Usage usage;
}