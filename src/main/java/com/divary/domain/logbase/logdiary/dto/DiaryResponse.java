package com.divary.domain.logbase.logdiary.dto;

import com.divary.domain.logbase.logdiary.entity.Diary;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
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

    public static DiaryResponse from(Diary diary, String processedContentJson, ObjectMapper objectMapper) {
        List<Map<String, Object>> contents;
        if (processedContentJson == null || processedContentJson.trim().isEmpty()) {
            contents = Collections.emptyList();
        } else {
            try {
                contents = objectMapper.readValue(
                        processedContentJson,
                        new TypeReference<>() {
                        }
                );
            } catch (JsonProcessingException e) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
        }

        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .logId(diary.getLogBaseInfo().getId())
                .contents(contents)
                .build();
    }
}
