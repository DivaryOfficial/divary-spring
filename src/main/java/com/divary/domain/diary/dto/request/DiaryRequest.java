package com.divary.domain.diary.dto.request;

import com.divary.domain.diary.enums.FontSize;
import com.divary.domain.diary.enums.FontType;
import com.divary.domain.diary.enums.TextAlign;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
@Schema(description = "일기 작성 DTO")
public class DiaryRequest {
    @Schema(description = "일기 본문", example = "오늘은 바다 거북이를 만난 날이다.")
    private String content;

    @Schema(description = "글씨체", example = "BASIC")
    private FontType fontType;

    @Schema(description = "글씨 크기", example = "SIZE_14")
    private FontSize fontSize;

    @Schema(description = "기울임 여부")
    private Boolean italic;

    @Schema(description = "밑줄 여부")
    private Boolean underline;

    @Schema(description = "취소선 여부")
    private Boolean strikethrough;

    @Schema(description = "정렬 방식", example = "LEFT")
    private TextAlign textAlign;

    @Schema(description = "Swagger UI에서는 한 장만 첨부되지만, 실제 요청에서는 여러 장 첨부 가능. \nSend empty value 체크 박스는 누르지 말아주세요",type = "string", format = "binary")
    private List<MultipartFile> images;

    @Schema(hidden = true)
    public List<MultipartFile> getImagesSafe() {
        return images == null ? Collections.emptyList() : images;
    }

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 손글씨 위치 등)
}

