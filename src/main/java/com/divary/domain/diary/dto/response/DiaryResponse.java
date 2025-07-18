package com.divary.domain.diary.dto.response;

import com.divary.domain.diary.entity.Diary;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.service.ImageStorageService;
import java.util.List;
import java.util.stream.Collectors;
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

    public static DiaryResponse from(Diary diary, ImageStorageService imageStorageService) {
        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .logId(diary.getLogBook().getId())
                .content(diary.getContent())
                .images(diary.getImages().stream()
                        .map(image -> ImageResponse.from(image,
                                imageStorageService.generatePublicUrl(image.getS3Key())))
                        .collect(Collectors.toList()))
                .build();
    }

}