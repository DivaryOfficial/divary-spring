package com.divary.domain.chatroom.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 생성 요청 (첫 메시지)")
public class ChatRoomCreateRequest {

    @NotBlank(message = "첫 메시지는 필수입니다.")
    @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다.")
    @Schema(description = "첫 메시지", example = "이 물고기가 뭔지 알 수 있을까요?")
    private String firstMessage;

    @Schema(description = "첨부 이미지 (선택사항)", type = "string", format = "binary")
    private MultipartFile image;
} 