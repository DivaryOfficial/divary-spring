package com.divary.domain.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Schema(description = "일기 수정 DTO")
public class DiaryUpdateRequest {

    @Schema(description = "수정할 일기 본문", example = "수정된 내용입니다.")
    private final String content;

    @Schema(description = "삭제할 이미지 ID 목록")
    private final List<Long> deleteImageIds;

    @Schema(description = "새로 추가할 이미지 리스트 ( 선택 사항 )", type = "array", format = "binary")
    private final List<MultipartFile> newImages;


    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 폰트, 크기, 손글씨 위치 등)

    @Builder
    public DiaryUpdateRequest(String content, List<Long> deleteImageIds, List<MultipartFile> newImages) {
        this.content = content;
        this.deleteImageIds = deleteImageIds == null ? null : List.copyOf(deleteImageIds);
        this.newImages = newImages == null ? null : List.copyOf(newImages);
    }
}

