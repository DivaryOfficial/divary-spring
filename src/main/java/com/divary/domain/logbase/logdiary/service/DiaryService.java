package com.divary.domain.logbase.logdiary.service;

import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.LogBaseInfoService;
import com.divary.domain.logbase.logdiary.dto.DiaryRequest;
import com.divary.domain.logbase.logdiary.dto.DiaryResponse;
import com.divary.domain.logbase.logdiary.entity.Diary;
import com.divary.domain.logbase.logdiary.repository.DiaryRepository;
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
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final LogBaseInfoService logBaseInfoService;
    private final ObjectMapper objectMapper;

    @Transactional
    public DiaryResponse createDiary(Long userId, Long logBaseInfoId, DiaryRequest request) {
        // 로그베이스의 존재와 접근 권한 확인
        LogBaseInfo logBaseInfo = logBaseInfoService.validateAccess(logBaseInfoId, userId);

        // 이후 다이어리 중복 여부 확인
        if (diaryRepository.existsByLogBaseInfoId(logBaseInfoId)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        String contentJson = toJson(request.getContents());
        Diary diary = Diary.builder()
                .logBaseInfo(logBaseInfo)
                .contentJson(contentJson)
                .build();

        diaryRepository.save(diary);
        return DiaryResponse.from(diary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long userId, Long logBaseInfoId, DiaryRequest request) {
        Diary diary = getDiaryWithAuth(logBaseInfoId, userId);
        String contentJson = toJson(request.getContents());
        diary.updateContent(contentJson);
        return DiaryResponse.from(diary);
    }

    @Transactional(readOnly = true)
    public DiaryResponse getDiary(Long userId, Long logBaseInfoId) {
        Diary diary = getDiaryWithAuth(logBaseInfoId, userId);
        return DiaryResponse.from(diary);
    }

    private String toJson(Object contents) {
        try {
            return objectMapper.writeValueAsString(contents);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }
    }


    private Diary getDiaryWithAuth(Long logBaseInfoId, Long userId) {
        // 다이어리를 update 하거나 get 할때, 로그북 베이스에 일기가 존재하는지 확인
        Diary diary = diaryRepository.findByLogBaseInfoId(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_NOT_FOUND));

        // 다이어리를 update 하거나 get 할때, 일기에 접근 권한이 있는지 확인
        if (!diary.getLogBaseInfo().getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_FORBIDDEN_ACCESS);
        }

        return diary;
    }

}