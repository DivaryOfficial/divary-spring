package com.divary.domain.logbook.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberServiceImpl;
import com.divary.domain.logbook.dto.request.CompanionRequestDTO;
import com.divary.domain.logbook.dto.request.LogBaseCreateRequestDTO;
import com.divary.domain.logbook.dto.request.LogDetailPutRequestDTO;
import com.divary.domain.logbook.dto.response.*;
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
    private void validateLogBookOwnership(LogBaseInfo logBaseInfo, Long userId) {
        if (!logBaseInfo.getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.LOG_ACCESS_DENIED);
        }
    }//로그인한 사용자가 자신의 로그북만 접근 가능하도록 검증

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
    public LogDetailCreateResultDTO createLogDetail(Long logBaseInfoId, Long userId) {

        LogBaseInfo base = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        validateLogBookOwnership(base,userId);

        // 연결된 기존의 로그북 개수 확인
        if (logBookRepository.countByLogBaseInfo(base) >= 3) {
            throw new BusinessException(ErrorCode.LOG_LIMIT_EXCEEDED);
        }//하루 최대 3개 넘으면 에러 던지기

        Member member = memberService.findById(userId);
        int accumulation = logBookRepository.countByLogBaseInfoMember(member)+1;
        //누적횟수 계산

        LogBook logBook = LogBook.builder()
                .logBaseInfo(base)
                .accumulation(accumulation)
                .build();

        logBookRepository.save(logBook);

        return new LogDetailCreateResultDTO(logBook.getId(),"세부 로그북 생성 완료");
    }

    @Transactional
    public void deleteLog(Long logBaseId,Long userId) {

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logBaseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        validateLogBookOwnership(logBaseInfo, userId);

        if (logBaseInfo.getLogBooks() == null || logBaseInfo.getLogBooks().isEmpty()) {
            throw new BusinessException(ErrorCode.LOG_NOT_FOUND);
        }

        logBaseInfoRepository.delete(logBaseInfo);
    }

    @Transactional
    public LogDetailPutResultDTO updateLogBook(Long userId, Long logBookId, LogDetailPutRequestDTO dto) {

        LogBook logBook = logBookRepository.findById(logBookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_NOT_FOUND));

        validateLogBookOwnership(logBook.getLogBaseInfo(),userId);

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


        LogBaseInfo base = logBaseInfoRepository.findById(dto.getLogBaseInfoId())
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        if (dto.getSaveStatus() == SaveStatus.TEMP){
            base.setSaveStatus(SaveStatus.TEMP);
            logBaseInfoRepository.save(base);
        }//로그 세부내용이 임시저장 상태면 로그베이스 저장상태를 임시저장으로 변환

        if (dto.getDate() != base.getDate()){
            base.setDate(dto.getDate());
            logBaseInfoRepository.save(base);
        }//처음 로그북 추가할 때의 날짜를 다시 변경하는 경우, 로그베이스의 날짜까지 다시 수정

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

        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(()->new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        validateLogBookOwnership(logBaseInfo,userId);

        logBaseInfo.updateName(name);

    }


}
