package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.request.DiaryUpdateRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {

    private final DiaryRepository diaryRepository;
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


        Diary.DiaryBuilder builder = Diary.builder()
                .logBook(logbook)
                .content(request.getContent());

        if (request.getContent() != null && !request.getContent().isBlank()) {
            builder
                    .fontType(request.getFontType())
                    .fontSize(request.getFontSize())
                    .italic(request.getItalic())
                    .underline(request.getUnderline())
                    .strikethrough(request.getStrikethrough())
                    .textAlign(request.getTextAlign());
        }

        Diary diary = builder.build();
        diaryRepository.save(diary);

        // 이미지 업로드 (경로 패턴: users/{userId}/diary/{logId})
        Long userId = getUserId();
        List<MultipartFile> images = request.getImagesSafe();
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

        return DiaryResponse.from(diary, imageService);

    }

    @Transactional
    public DiaryResponse getDiary(Long logId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        return DiaryResponse.from(diary, imageService);
    }
}
