package com.divary.domain.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@Schema(description = "일기 작성 DTO")
public class DiaryRequest {
    @NotNull
    @Schema(
            description = "일기 콘텐츠 JSON 문자열 (RTF Base64, 이미지 URL, 드로잉 포함)",
            example = "[{ \"type\": \"text\", \"rtfData\": \"...\" }, { \"type\": \"image\", \"data\": { \" https://divary-file-bucket.s3....\" } }]"
    )
    private String contents;
}

