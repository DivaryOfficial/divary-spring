package com.divary.domain.diary.dto.response;

import com.divary.domain.diary.entity.Diary;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponse {
    private Long diaryId;
    private Long logId;
    private String contents;

    public static DiaryResponse from(Diary diary) {
        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .logId(diary.getLogBook().getId())
                .contents(diary.getContentJson())
                .build();
    }
}