package com.divary.domain.diary.dto.request;

import com.divary.domain.diary.enums.FontSize;
import com.divary.domain.diary.enums.FontType;
import com.divary.domain.diary.enums.TextAlign;
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

    @Schema(description = "글씨체", example = "BASIC")
    private FontType fontType;

    @Schema(description = "글씨 크기", example = "SIZE_14")
    private FontSize fontSize;

    @Schema(description = "기울임 여부")
    private boolean italic;

    @Schema(description = "밑줄 여부")
    private boolean underline;

    @Schema(description = "취소선 여부")
    private boolean strikethrough;

    @Schema(description = "정렬 방식", example = "LEFT")
    private TextAlign textAlign;

    @Schema(description = "삭제할 이미지 ID 목록")
    private final List<Long> deleteImageIds;

    @Schema(description = "새로 추가할 이미지 리스트 ( 선택 사항 )", type = "array", format = "binary")
    private final List<MultipartFile> newImages;

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 손글씨 위치 등)

    @Builder
    public DiaryUpdateRequest(String content, FontType fontType, FontSize fontSize,
                              Boolean italic, Boolean underline, Boolean strikethrough,
                              TextAlign textAlign, List<Long> deleteImageIds, List<MultipartFile> newImages) {
        this.content = content;
        this.fontType = fontType;
        this.fontSize = fontSize;
        this.italic = italic;
        this.underline = underline;
        this.strikethrough = strikethrough;
        this.textAlign = textAlign;
        this.deleteImageIds = deleteImageIds == null ? null : List.copyOf(deleteImageIds);
        this.newImages = newImages == null ? null : List.copyOf(newImages);
    }
}

