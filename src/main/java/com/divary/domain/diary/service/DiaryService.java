package com.divary.domain.diary.service;

import com.divary.domain.diary.dto.DiaryRequest;
import com.divary.domain.diary.dto.DiaryResponse;
import com.divary.domain.diary.entity.Diary;
import com.divary.domain.diary.repository.DiaryRepository;
import com.divary.domain.logbook.entity.LogBaseInfo;
import com.divary.domain.logbook.repository.LogBaseInfoRepository;
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
    private final LogBaseInfoRepository logBaseInfoRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public DiaryResponse createDiary(Long userId, Long logId, DiaryRequest request) {
        if (diaryRepository.existsByLogBaseInfoId(logId)) {
            throw new BusinessException(ErrorCode.DIARY_ALREADY_EXISTS);
        }

        LogBaseInfo logBaseInfo = getLogBaseInfoWithAuth(logId, userId);

        String contentJson = toJson(request.getContents());
        Diary diary = Diary.builder()
                .logBaseInfo(logBaseInfo)
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

    private LogBaseInfo getLogBaseInfoWithAuth(Long logId, Long userId) {
        // 로그북 베이스가 존재하는지 확인
        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        // 로그북 베이스를 작성한 유저가 일기 작성 요청을 보내는 유저인지 확인
        if (!logBaseInfo.getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.DIARY_FORBIDDEN_ACCESS);
        }

        return logBaseInfo;
    }

    private Diary getDiaryWithAuth(Long logId, Long userId) {
        return diaryRepository.findByLogBaseInfoIdAndMemberId(logId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DIARY_FORBIDDEN_ACCESS));
    }

}