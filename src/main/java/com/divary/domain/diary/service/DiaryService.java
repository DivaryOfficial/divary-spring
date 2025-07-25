package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.request.DiaryRequest;
import com.divary.domain.diary.dto.response.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final LogBookRepository logBookRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DiaryResponse createDiary(Long userId, Long logId, DiaryRequest request) {
        if (diaryRepository.existsByLogBookId(logId)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        LogBook logbook = logBookRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        if (!logbook.getLogBaseInfo().getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_FORBIDDEN_ACCESS);
        }

        String contentJson = toJson(request.getContents());
        Diary diary = Diary.builder()
                .logBook(logbook)
                .contentJson(contentJson)
                .build();

        diaryRepository.save(diary);
        return DiaryResponse.from(diary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long userId, Long logId, DiaryRequest request) {
        Diary diary = getDiaryWithAuth(logId, userId);
        String contentJson = toJson(request.getContents());
        diary.updateContent(contentJson);
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(Long userId, Long logId) {
        Diary diary = getDiaryWithAuth(logId, userId);
        return DiaryResponse.from(diary);
    }

    private String toJson(Object contents) {
        try {
            return objectMapper.writeValueAsString(contents);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }
    }

    private Diary getDiaryWithAuth(Long logId, Long userId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        if (!diary.getLogBook().getLogBaseInfo().getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_FORBIDDEN_ACCESS);
        }

        return diary;
    }

}
