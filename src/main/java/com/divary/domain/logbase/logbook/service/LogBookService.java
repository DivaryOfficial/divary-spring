package com.divary.domain.logbase.logbook.service;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.service.MemberServiceImpl;
import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.LogBaseInfoRepository;
import com.divary.domain.logbase.logbook.dto.request.*;
import com.divary.domain.logbase.logbook.dto.response.*;
import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.logbook.entity.LogBook;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logbook.repository.CompanionRepository;
import com.divary.domain.logbase.logbook.repository.LogBookRepository;
import com.divary.domain.logbase.logbook.dto.response.LogBaseListResultDTO;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogBookService {

    private final LogBaseInfoRepository logBaseInfoRepository;
    private final LogBookRepository logBookRepository;
    private final CompanionRepository companionRepository;
    private final MemberServiceImpl memberService;

    @Transactional
    public LogBaseCreateResultDTO createLogBase
            (@Valid LogBaseCreateRequestDTO request, Long userId) {

        Member member = memberService.findById(userId);

        LogBaseInfo logBaseInfo = LogBaseInfo.builder()
                .iconType(request.getIconType())
                .name(request.getName())
                .member(member)
                .date(request.getDate())
                .saveStatus(SaveStatus.COMPLETE)
                .build();

        LogBaseInfo saved = logBaseInfoRepository.save(logBaseInfo);

        return LogBaseCreateResultDTO.builder()
                .name(saved.getName())
                .iconType(saved.getIconType())
                .date(saved.getDate())
                .LogBaseInfoId(saved.getId())
                .build();
    }

    @Transactional
    public List<LogBaseListResultDTO> getLogBooksByYearAndStatus(int year, SaveStatus status, Long userId) {

        List<LogBaseInfo> logBaseInfoList;

        Member member = memberService.findById(userId);

        if (status == null) {
            // 쿼리스트링 없을 경우 전체 조회
            logBaseInfoList = logBaseInfoRepository.findByYearAndMember(year,member);
        } else {
            logBaseInfoList = logBaseInfoRepository.findByYearAndStatusAndMember(year,status,member);
        }

        return logBaseInfoList.stream()
                .map(logBaseInfo -> LogBaseListResultDTO.builder()
                        .name(logBaseInfo.getName())
                        .date(logBaseInfo.getDate())
                        .iconType(logBaseInfo.getIconType())
                        .saveStatus(logBaseInfo.getSaveStatus())
                        .LogBaseInfoId(logBaseInfo.getId())
                        .build())
                .collect(Collectors.toList());

    }//연도에 따라, 저장 상태(임시저장,완전저장)에 따라 로그북베이스정보 조회

    @Transactional
    public List<LogBaseListResultDTO> getLogBooks(Long userId) {

        Member member = memberService.findById(userId);

        List<LogBaseInfo> logBaseInfoList = logBaseInfoRepository.findByMemberId(userId);

        return logBaseInfoList.stream()
                .map(logBaseInfo -> LogBaseListResultDTO.builder()
                        .name(logBaseInfo.getName())
                        .date(logBaseInfo.getDate())
                        .iconType(logBaseInfo.getIconType())
                        .saveStatus(logBaseInfo.getSaveStatus())
                        .LogBaseInfoId(logBaseInfo.getId())
                        .build())
                .collect(Collectors.toList());

    }//특정 유저의 전체 로그북베이스정보 리스트 조회


    @Transactional
    public List<LogBookDetailResultDTO> getLogDetail(Long logBaseInfoId, Long userId) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        List<LogBook> logBooks = logBookRepository.findByLogBaseInfo(logBaseInfo);

        if (logBooks.isEmpty()) {
            throw new BusinessException(ErrorCode.LOG_NOT_FOUND);
        }

        Integer accumulation
                = logBookRepository.countByLogBaseInfoMemberIdAndSaveStatus(userId,SaveStatus.COMPLETE);
        //현재기준으로 총 로그북 누적횟수 계산

        // 각 로그북에 대해 companion 함께 매핑하여 DTO 변환
        return logBooks.stream()
                .map(logBook -> LogBookDetailResultDTO.from(logBook, logBook.getCompanions(), accumulation))
                .collect(Collectors.toList());
    }

    @Transactional
    public LogDetailCreateResultDTO createLogDetail(Long logBaseInfoId, Long userId) {

        LogBaseInfo base = logBaseInfoRepository.findByIdAndMemberId(logBaseInfoId,userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        // 연결된 기존의 로그북 개수 확인
        if (logBookRepository.countByLogBaseInfo(base) >= 3) {
            throw new BusinessException(ErrorCode.LOG_LIMIT_EXCEEDED);
        }//하루 최대 3개 넘으면 에러 던지기

        LogBook logBook = LogBook.builder()
                .logBaseInfo(base)
                .build();

        logBookRepository.save(logBook);

        return new LogDetailCreateResultDTO(logBook.getId(),"세부 로그북 생성 완료");
    }

    @Transactional
    public void deleteLog(Long logBaseId,Long userId) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findByIdAndMemberId(logBaseId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));


        if (logBaseInfo.getLogBooks() == null || logBaseInfo.getLogBooks().isEmpty()) {
            throw new BusinessException(ErrorCode.LOG_NOT_FOUND);
        }

        logBaseInfoRepository.delete(logBaseInfo);
    }

    @Transactional
    public LogDetailPutResultDTO updateLogBook(Long userId, Long logBookId, LogDetailPutRequestDTO dto) {

        LogBook logBook = logBookRepository.findByIdAndLogBaseInfoMemberId(logBookId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));


        // 모든 필드 덮어쓰기 (null도 그대로 반영)
        logBook.setPlace(dto.getPlace());
        logBook.setSaveStatus(dto.getSaveStatus());
        logBook.setDivePoint(dto.getDivePoint());
        logBook.setDiveMethod(dto.getDiveMethod());
        logBook.setDivePurpose(dto.getDivePurpose());
        logBook.setSuitType(dto.getSuitType());
        logBook.setEquipment(dto.getEquipment());
        logBook.setWeight(dto.getWeight());
        logBook.setPerceivedWeight(dto.getPerceivedWeight());
        logBook.setDiveTime(dto.getDiveTime());
        logBook.setMaxDepth(dto.getMaxDepth());
        logBook.setAvgDepth(dto.getAvgDepth());
        logBook.setDecompressDepth(dto.getDecompressDepth());
        logBook.setDecompressTime(dto.getDecompressTime());
        logBook.setStartPressure(dto.getStartPressure());
        logBook.setFinishPressure(dto.getFinishPressure());
        logBook.setConsumption(dto.getConsumption());
        logBook.setWeatherType(dto.getWeather());
        logBook.setWind(dto.getWind());
        logBook.setTide(dto.getTide());
        logBook.setWave(dto.getWave());
        logBook.setTemperature(dto.getTemperature());
        logBook.setWaterTemperature(dto.getWaterTemperature());
        logBook.setPerceivedTemp(dto.getPerceivedTemp());
        logBook.setSight(dto.getSight());


        LogBaseInfo base = logBook.getLogBaseInfo();

        if (dto.getSaveStatus() == SaveStatus.TEMP){
            base.setSaveStatus(SaveStatus.TEMP);
            logBaseInfoRepository.save(base);
        }//로그 세부내용이 임시저장 상태면 로그베이스 저장상태를 임시저장으로 변환


        // Companion 덮어쓰기 (기존 삭제 후 다시 저장)
        companionRepository.deleteByLogBook(logBook);
        if (dto.getCompanions() != null) {
            for (CompanionRequestDTO c : dto.getCompanions()) {
                Companion companion = Companion.builder()
                        .logBook(logBook)
                        .name(c.getName())
                        .type(c.getType())
                        .build();
                companionRepository.save(companion);
            }
        }

        return new LogDetailPutResultDTO(logBook.getId(), "로그북이 수정되었습니다.");
    }

    @Transactional
    public void updateLogName(Long logBaseInfoId, Long userId, String name){

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findByIdAndMemberId(logBaseInfoId, userId)
                .orElseThrow(()->new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        logBaseInfo.updateName(name);

    }

    public LogExistResultDTO checkLogExists(LocalDate date, Long userId) {

        Optional<LogBaseInfo> logBase = logBaseInfoRepository.findByDateAndMemberId(date, userId);

        if (logBase.isPresent()) {
            return LogExistResultDTO.builder()
                    .exists(true)
                    .logBaseInfoId(logBase.get().getId())
                    .build();
        }
        return LogExistResultDTO.builder()
                .exists(false)
                .logBaseInfoId(null)
                .build();

    }

    @Transactional
    public void updateLogDate(Long logBaseInfoId, Long userId, LocalDate date) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findByIdAndMemberId(logBaseInfoId, userId)
                .orElseThrow(()->new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        if (Objects.equals(date,logBaseInfo.getDate())){
            return;
        }
        Optional<LogBaseInfo> existing = logBaseInfoRepository.findByDateAndMemberId(date,userId);
        if (existing.isPresent() && !existing.get().getId().equals(logBaseInfoId))
        {
            throw new BusinessException(ErrorCode.LOG_BASE_ALREADY_EXISTS);
        }
        logBaseInfo.setDate(date);
        logBaseInfoRepository.save(logBaseInfo);
        //처음 로그북 추가할 때의 날짜를 다시 변경하는 경우, 로그베이스의 날짜까지 다시 수정

    }

}
