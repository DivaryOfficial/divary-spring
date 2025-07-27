package com.divary.domain.mypage.dto.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class MyPageImageRequestDTO {
    @Schema(description = "자격증 이미지", example = "이미지 파일을 보내주세요", nullable = false)
    private MultipartFile image;
}
