package com.divary.domain.logbase.logbook.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberServiceImpl;
import com.divary.domain.logbase.logbook.dto.request.CompanionRequestDTO;
import com.divary.domain.logbase.logbook.dto.request.LogBaseCreateRequestDTO;
import com.divary.domain.logbase.logbook.dto.request.LogDetailCreateRequestDTO;
import com.divary.domain.logbase.logbook.dto.response.CompanionResultDTO.LogBaseListResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogBookDetailResultDTO.LogBaseCreateResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogBookDetailResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogDetailCreateResultDTO;
import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.logbook.entity.LogBook;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logbook.repository.CompanionRepository;
import com.divary.domain.logbase.LogBaseInfoRepository;
import com.divary.domain.logbase.logbook.repository.LogBookRepository;
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

    private final LogBaseInfoRepository logBaseInfoRepository;
    private final LogBookRepository logBookRepository;
    private final CompanionRepository companionRepository;
    private final MemberServiceImpl memberService;

    @Transactional
    public LogBaseCreateResultDTO createLogBase(@Valid LogBaseCreateRequestDTO request) {

        Member member = memberService.findById(1L);//임시로 데이터 넣음

        LogBaseInfo logBaseInfo = LogBaseInfo.builder()
                .iconType(request.getIconType())
                .name(request.getName())
                .member(member)
                .date(request.getDate())
                .saveStatus(SaveStatus.COMPLETE)
                .build();

        LogBaseInfo saved = logBaseInfoRepository.save(logBaseInfo);

        int accumulation = logBookRepository.countByLogBaseInfoMember(member)+1;
        //그동안의 로그북 누적횟수 세기

        return LogBaseCreateResultDTO.builder()
                .name(saved.getName())
                .iconType(saved.getIconType())
                .date(saved.getDate())
                .accumulation(accumulation)
                .LogBaseInfoId(saved.getId())
                .build();
    }

    @Transactional
    public List<LogBaseListResultDTO> getLogBooksByYearAndStatus(int year, SaveStatus status) {

        List<LogBaseInfo> logBaseInfoList;

        if (status == null) {
            // 쿼리스트링 없을 경우 전체 조회
            logBaseInfoList = logBaseInfoRepository.findByYear(year);
        } else {
            logBaseInfoList = logBaseInfoRepository.findByYearAndStatus(year,status);
        }

        return logBaseInfoList.stream()
                .map(logBaseInfo -> LogBaseListResultDTO.builder()
                        .name(logBaseInfo.getName())
                        .date(logBaseInfo.getDate())
                        .iconType(logBaseInfo.getIconType())
                        .LogBaseInfoId(logBaseInfo.getId())
                        .build())
                .collect(Collectors.toList());

    }//연도에 따라, 저장 상태(임시저장,완전저장)에 따라 로그북베이스정보 조회

    @Transactional
    public List<LogBookDetailResultDTO> getLogDetail(Long logBaseInfoId) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

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

    @Transactional
    public LogDetailCreateResultDTO createLogDetail(LogDetailCreateRequestDTO dto, Long logBaseInfoId) {

        LogBaseInfo base = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        // 연결된 기존의 로그북 개수 확인
        if (logBookRepository.countByLogBaseInfo(base) >= 3) {
            throw new BusinessException(ErrorCode.LOG_LIMIT_EXCEEDED);
        }//하루 최대 3개 넘으면 에러 던지기

        if (dto.getSaveStatus() == SaveStatus.TEMP){
            base.setSaveStatus(SaveStatus.TEMP);
            logBaseInfoRepository.save(base);
        }//로그 세부내용이 임시저장 상태면 로그베이스 저장상태를 임시저장으로 변환

        if (dto.getDate() != base.getDate()){
            base.setDate(dto.getDate());
            logBaseInfoRepository.save(base);
        }//처음 로그북 추가할 때의 날짜를 다시 변경하는 경우, 로그베이스의 날짜까지 다시 수정

        Member member = memberService.findById(1L);//임시로 데이터 넣음
        int accumulation = logBookRepository.countByLogBaseInfoMember(member)+1;
        //누적횟수 계산

        LogBook logBook = LogBook.builder()
                .logBaseInfo(base)
                .accumulation(accumulation)
                .place(dto.getPlace())
                .divePoint(dto.getDivePoint())
                .diveMethod(dto.getDiveMethod())
                .divePurpose(dto.getDivePurpose())
                .suitType(dto.getSuitType())
                .equipment(dto.getEquipment())
                .weight(dto.getWeight())
                .perceivedWeight(dto.getPerceivedWeight())
                .weatherType(dto.getWeather())
                .wind(dto.getWind())
                .tide(dto.getTide())
                .wave(dto.getWave())
                .temperature(dto.getTemperature())
                .waterTemperature(dto.getWaterTemperature())
                .perceivedTemp(dto.getPerceivedTemp())
                .sight(dto.getSight())
                .diveTime(dto.getDiveTime())
                .maxDepth(dto.getMaxDepth())
                .avgDepth(dto.getAvgDepth())
                .decompressDepth(dto.getDecompressDepth())
                .decompressTime(dto.getDecompressTime())
                .startPressure(dto.getStartPressure())
                .finishPressure(dto.getFinishPressure())
                .consumption(dto.getConsumption())
                .build();

        logBookRepository.save(logBook);

        if (dto.getCompanions() != null) {
            for (CompanionRequestDTO c : dto.getCompanions()) {
                Companion companion = Companion.builder()
                        .name(c.getCompanion())
                        .type(c.getType())
                        .logBook(logBook)
                        .build();
                companionRepository.save(companion);
            }
        }

        return new LogDetailCreateResultDTO(logBook.getId(),"로그 세부내용 저장 완료");
    }


}
