package com.divary.domain.logbook.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberServiceImpl;
import com.divary.domain.logbook.dto.request.LogBookCreateRequestDTO;
import com.divary.domain.logbook.dto.response.LogBookCreateResultDTO;
import com.divary.domain.logbook.dto.response.LogBookDetailResultDTO;
import com.divary.domain.logbook.dto.response.LogBookListResultDTO;
import com.divary.domain.logbook.entity.Companion;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.enums.saveStatus;
import com.divary.domain.logbook.repository.CompanionRepository;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogBookService {

    private final LogBookRepository logBookRepository;
    private final CompanionRepository companionRepository;
    private final MemberServiceImpl memberService;

    @Transactional
    public LogBookCreateResultDTO createLog(@Valid LogBookCreateRequestDTO request) {

        Member member = memberService.findById(request.getMemberId());

        int count = logBookRepository.countByMember(member)+1; // 기존 개수

        LogBook logBook = LogBook.builder()
                .iconType(request.getIconType())
                .name(request.getName())
                .date(request.getDate()) // 현재 날짜로 설정
                .accumulation(count)
                .member(member)
                .build();

        LogBook saved = logBookRepository.save(logBook);

        return LogBookCreateResultDTO.builder()
                .name(saved.getName())
                .iconType(saved.getIconType())
                .date(saved.getDate())
                .accumulation(saved.getAccumulation())
                .build();
    }

    @Transactional
    public List<LogBookListResultDTO> getLogBooksByYearAndStatus(int year, saveStatus status) {
        List<LogBook> logBooks = logBookRepository.findByYearAndStatus(year,status);

        return logBooks.stream()
                .map(logBook -> LogBookListResultDTO.builder()
                        .name(logBook.getName())
                        .date(logBook.getDate())
                        .iconType(logBook.getIconType())
                        .build())
                .collect(Collectors.toList());
    }//연도에 따라, 저장 상태(임시저장,완전저장)에 따라 로그북 조회

    @Transactional
    public LogBookDetailResultDTO getLogDetail(Long logId) {
        LogBook logBook = logBookRepository.findById(logId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        List<Companion> companions = companionRepository.findByLogBookId(logId);

        return LogBookDetailResultDTO.from(logBook, companions);
    }



}
