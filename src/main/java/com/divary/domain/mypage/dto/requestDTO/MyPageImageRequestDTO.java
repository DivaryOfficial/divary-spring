package com.divary.domain.mypage.dto.requestDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class MyPageImageRequestDTO {
    @Schema(description = "자격증 이미지")
    @NotNull
    private MultipartFile image;
}
