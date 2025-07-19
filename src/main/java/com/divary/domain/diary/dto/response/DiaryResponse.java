package com.divary.domain.diary.dto.response;

import com.divary.domain.diary.entity.Diary;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.service.ImageService;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiaryResponse {
    private Long diaryId;
    private Long logId;
    private String content;
    private List<ImageResponse> images;

    // TODO: 프론트에서 스펙 전달해주면 필드 추가 예정 (ex. 폰트, 크기, 손글씨 위치 등)

    public static DiaryResponse from(Diary diary, ImageService imageService) {
        Long userId = 1L; // TODO: 유저 인증 붙이면 수정할 것
        Long logId=diary.getLogBook().getId();

        String pathPrefix = String.format("users/%d/diary/%d/", userId, logId);

        List<ImageResponse> images = imageService.getImagesByPath(pathPrefix);

        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .logId(logId)
                .content(diary.getContent())
                .images(images)
                .build();
    }

}