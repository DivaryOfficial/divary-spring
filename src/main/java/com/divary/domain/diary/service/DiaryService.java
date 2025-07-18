package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.image.dto.response.ImageResponse;
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
    private final LogBookRepository logBookRepository;
    private final ImageService imageService;
    private final ImageStorageService imageStorageService;

    // 사용자 ID 가져오기
    private Long getUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    @Transactional
    public DiaryResponse createDiary(Long logId, DiaryRequest request) {
        Long userId = getUserId();

        LogBook logBook = logBookRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        Diary diary = new Diary(logBook, request.getContent());
        diaryRepository.save(diary);

        List<MultipartFile> imageFiles = request.getImages();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                // 다이어리 이미지 업로드 (USER_DIARY 타입으로)
                ImageResponse imageResponse = imageService.uploadImageByType(
                        ImageType.USER_DIARY,
                        imageFile,
                        userId,
                        "diary/" + diary.getId());

                log.debug("Uploaded diary image: {}", imageResponse.getFileUrl());
            }
        }

        return getDiary(logId);
    }

    @Transactional
    public DiaryResponse updateDiary(Long logId, DiaryUpdateRequest request) {
        Long userId = getUserId();

        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (request.getContent() != null) {
            diary.updateContent(request.getContent());
        }

        // 이미지 삭제 처리
        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            List<Long> deleteImageIds = request.getDeleteImageIds();

            // 이미지 서비스를 통해 S3에서 이미지 삭제
            for (Long imageId : deleteImageIds) {
                try {
                    imageService.deleteImage(imageId);
                    log.debug("Deleted image with ID: {}", imageId);
                } catch (Exception e) {
                    log.error("Failed to delete image with ID: {}", imageId, e);
                }
            }

            // 엔티티에서 이미지 참조 제거
            diary.getImages().removeIf(image -> deleteImageIds.contains(image.getId()));
        }

        // 새 이미지 추가 처리
        List<MultipartFile> newImageFiles = request.getNewImages();
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile imageFile : newImageFiles) {
                // 다이어리 이미지 업로드 (USER_DIARY 타입으로)
                ImageResponse imageResponse = imageService.uploadImageByType(
                        ImageType.USER_DIARY,
                        imageFile,
                        userId,
                        "diary/" + diary.getId());

                log.debug("Added new diary image: {}", imageResponse.getFileUrl());
            }
        }

        return getDiary(logId);
    }

    @Transactional
    public DiaryResponse getDiary(Long logId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponse.from(diary, imageStorageService);
    }
}
