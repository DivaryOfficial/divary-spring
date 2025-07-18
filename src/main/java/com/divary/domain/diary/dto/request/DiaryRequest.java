package com.divary.domain.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Getter;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Schema(description = "일기 작성 DTO")
public class DiaryRequest {
    @Schema(description = "일기 본문", example = "오늘은 바다 거북이를 만난 날이다.")
    private String content;

    @Schema(description = "첨부 이미지 리스트 ( 선택 사항 )", type = "array", format = "binary")
    private List<MultipartFile> images;

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 폰트, 크기, 손글씨 위치 등)
}

