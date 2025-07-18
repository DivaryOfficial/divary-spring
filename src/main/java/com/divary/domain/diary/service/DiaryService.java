package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.Image;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.image.service.ImageStorageService;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final LogBookRepository logBookRepository;
    private final ImageService imageService;
    private final ImageStorageService imageStorageService;

    // 현재 사용자 ID 가져오기
    private Long getCurrentUserId() {
        // TODO: 사용자 ID를 Authorization 헤더에서 가져오도록 수정
        return 1L;
    }

    @Transactional
    public void createDiary(Long logId, DiaryRequest request) {
        Long userId = getCurrentUserId();

        LogBook logBook = logBookRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        Diary diary = new Diary(logBook, request.getContent());
        diaryRepository.save(diary);

        List<MultipartFile> imageFiles = request.getImages();
        if (imageFiles != null && !imageFiles.isEmpty()) {

        }
    }

    @Transactional
    public void updateDiary(Long logId, DiaryUpdateRequest request) {
        Long userId = getCurrentUserId(); // ⭐️ 누락돼 있던 userId 추가

        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (request.getContent() != null) {
            diary.updateContent(request.getContent());
        }

        if (request.getDeleteImageIds() != null) {
            diary.getImages().removeIf(image -> request.getDeleteImageIds().contains(image.getId()));
        }

        List<MultipartFile> newImageFiles = request.getNewImages();
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
          
        }
    }

    @Transactional
    public DiaryResponse getDiary(Long logId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponse.from(diary, imageStorageService);
    }
}
