package com.divary.domain.diary.dto.response;

import com.divary.domain.diary.entity.Diary;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponse {
    private Long diaryId;
    private Long logId;
    private List<Map<String, Object>> contents;

    public static DiaryResponse from(Diary diary) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> contents;
        try {
            contents = objectMapper.readValue(
                    diary.getContentJson(),
                    new TypeReference<>() {
                    }
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }

        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .logId(diary.getLogBook().getId())
                .contents(contents)
                .build();
    }
}