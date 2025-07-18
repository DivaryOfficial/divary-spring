package com.divary.domain.logbook.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberServiceImpl;
import com.divary.domain.logbook.dto.request.LogBookCreateRequestDTO;
import com.divary.domain.logbook.dto.response.LogBaseListResultDTO;
import com.divary.domain.logbook.dto.response.LogBookCreateResultDTO;
import com.divary.domain.logbook.dto.response.LogBookDetailResultDTO;
import com.divary.domain.logbook.entity.Companion;
import com.divary.domain.logbook.entity.LogBaseInfo;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.enums.SaveStatus;
import com.divary.domain.logbook.repository.CompanionRepository;
import com.divary.domain.logbook.repository.LogBaseInfoRepository;
import com.divary.domain.logbook.repository.LogBookRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogBookService {

    private final LogBaseInfoRepository logBaseInfoRepository;
    private final LogBookRepository logBookRepository;
    private final CompanionRepository companionRepository;
    private final MemberServiceImpl memberService;

    @Transactional
    public LogBookCreateResultDTO createLog(@Valid LogBookCreateRequestDTO request) {

        Member member = memberService.findById(1L);//임시로 데이터 넣음

        LogBaseInfo logBaseInfo = LogBaseInfo.builder()
                .iconType(request.getIconType())
                .name(request.getName())
                .member(member)
                .date(request.getDate())
                .saveStatus(SaveStatus.TEMP)
                .build();

        LogBaseInfo saved = logBaseInfoRepository.save(logBaseInfo);

        int accumulation = logBookRepository.countByLogBaseInfoMember(member)+1;
        //그동안의 로그북 누적횟수 세기

        return LogBookCreateResultDTO.builder()
                .name(saved.getName())
                .iconType(saved.getIconType())
                .date(saved.getDate())
                .accumulation(accumulation)
                .build();
    }

    @Transactional
    public List<LogBaseListResultDTO> getLogBooksByYearAndStatus(int year, SaveStatus status) {

        List<LogBaseInfo> logBaseInfoList;

        if (status == null) {
            // 쿼리스트링 없을 경우 전체 조회
            logBaseInfoList = logBaseInfoRepository.findByYear(year); // 또는 전체 조건 없이 조회
        } else {
            logBaseInfoList = logBaseInfoRepository.findByYearAndStatus(year,status);
        }

        return logBaseInfoList.stream()
                .map(logBaseInfo -> LogBaseListResultDTO.builder()
                        .name(logBaseInfo.getName())
                        .date(logBaseInfo.getDate())
                        .iconType(logBaseInfo.getIconType())
                        .build())
                .collect(Collectors.toList());

    }//연도에 따라, 저장 상태(임시저장,완전저장)에 따라 로그북베이스정보 조회

    @Transactional
    public List<LogBookDetailResultDTO> getLogDetail(LocalDate date) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findByDate(date);

        if (logBaseInfo == null) {
            throw new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND);
        }

        List<LogBook> logBooks = logBookRepository.findByLogBaseInfo(logBaseInfo);

        if (logBooks.isEmpty()) {
            throw new BusinessException(ErrorCode.LOG_NOT_FOUND);
        }

        // 각 로그북에 대해 companion 함께 매핑하여 DTO 변환
        return logBooks.stream()
                .map(logBook -> {
                    List<Companion> companions = companionRepository.findByLogBook(logBook);
                    return LogBookDetailResultDTO.from(logBook, companions);
                })
                .collect(Collectors.toList());
    }

}
