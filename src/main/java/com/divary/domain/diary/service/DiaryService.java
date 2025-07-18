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

        // contents ->  JSON 문자열로
        String contentJson;
        try {
            contentJson = new ObjectMapper().writeValueAsString(request.getContents());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }

        Diary diary = Diary.builder()
                .logBook(logbook)
                .contentJson(contentJson)
                .build();

        diaryRepository.save(diary);
        return DiaryResponse.from(diary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long logId, DiaryRequest request) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        String contentJson;
        try {
            contentJson = new ObjectMapper().writeValueAsString(request.getContents());
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }

        diary.updateContent(contentJson);
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(Long logId) {
        Diary diary = diaryRepository.findByLogBookId(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));
        return DiaryResponse.from(diary);
    }

}
