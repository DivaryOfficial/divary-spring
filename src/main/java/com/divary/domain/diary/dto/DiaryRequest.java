package com.divary.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.List;
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
            description = "일기 콘텐츠 JSON 배열 (텍스트, 이미지, 드로잉 포함)",
            example = """
                    [
                      {
                        "type": "text",
                        "rtfData": "e1xydGYx..."
                      },
                      {
                        "type": "image",
                        "data": {
                          "s3Filename": "https://divary-file-bucket.s3....",
                          "caption": "바다 거북이와 함께",
                          "frameColor": "0",
                          "date": "2025-07-24"
                        }
                      }
                    ]
                    """)
    private List<Map<String, Object>> contents;

}