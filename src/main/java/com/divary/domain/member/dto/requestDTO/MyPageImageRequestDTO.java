package com.divary.domain.member.dto.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class MyPageImageRequestDTO {
    @Schema(description = "이미지", nullable = false)
    MultipartFile image;
}
