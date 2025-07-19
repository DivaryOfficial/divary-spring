package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.image.service.ImageStorageService;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final ImageStorageService imageStorageService;
    private final LogBookRepository logBookRepository;
    private final ImageService imageService;

    // 사용자 ID 가져오기
    private Long getUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    @Transactional
    public DiaryResponse createDiary(Long logId, DiaryRequest request) {
        if (diaryRepository.existsByLogBookId(logId)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        LogBook logbook = logBookRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        // Diary 엔티티 생성 content가 null이어도 허용
        Diary diary = Diary.builder()
                .logBook(logbook)
                .content(request.getContent())
                .build();
        diaryRepository.save(diary);

        // 이미지 업로드 (경로 패턴: users/{userId}/diary/{logId})
        Long userId = getUserId();
        List<MultipartFile> images = request.getImages();
        if (images != null) {
            for (MultipartFile image : images) {
                imageService.uploadImageByType(
                        ImageType.USER_DIARY,
                        image,
                        userId,
                        String.valueOf(logId)
                );
            }
        }

        return DiaryResponse.from(diary, imageService);
    }

    @Transactional
    public DiaryResponse updateDiary(Long logId, DiaryUpdateRequest request) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        diary.update(request.getContent());

        // 기존 이미지 모두 삭제
        List<ImageResponse> existingImages = imageService.getImagesByType(
                ImageType.USER_DIARY,
                getUserId(),
                String.valueOf(logId)
        );

        for (ImageResponse image : existingImages) {
            imageService.deleteImage(image.getId());
        }

        // 새 이미지 업로드
        for (MultipartFile file : request.getNewImages()) {
            imageService.uploadImageByType(
                    ImageType.USER_DIARY,
                    file,
                    getUserId(),
                    String.valueOf(logId)
            );
        }

        return DiaryResponse.from(diary, imageService);

    }

    @Transactional
    public DiaryResponse getDiary(Long logId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponse.from(diary, imageService);
    }
}
