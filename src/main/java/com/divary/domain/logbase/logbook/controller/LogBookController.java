package com.divary.domain.logbase.logbook.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbase.logbook.dto.request.LogBaseCreateRequestDTO;
import com.divary.domain.logbase.logbook.dto.request.LogDetailCreateRequestDTO;
import com.divary.domain.logbase.logbook.dto.response.CompanionResultDTO.LogBaseListResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogBookDetailResultDTO.LogBaseCreateResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogBookDetailResultDTO;
import com.divary.domain.logbase.logbook.dto.response.LogDetailCreateResultDTO;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logbook.service.LogBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Log API", description = "로그북 관련 API")
public class LogBookController {

    private final LogBookService logBookService;

    @PostMapping
    @Operation(summary = "초기 로그 생성", description = "다이빙 로그를 생성합니다.")
    public ApiResponse<LogBaseCreateResultDTO> createLog
            (@RequestBody @Valid LogBaseCreateRequestDTO createDTO)
    {
        LogBaseCreateResultDTO responseDto = logBookService.createLogBase(createDTO);
        return ApiResponse.success(responseDto);
    }

    @GetMapping
    @Operation(summary = "로그 리스트 조회", description = "연도와 저장 상태에 따라 로그북 리스트를 조회합니다.")
    public ApiResponse<List<LogBaseListResultDTO>> getLogsByYearAndStatus(
            @RequestParam int year,
            @RequestParam(required = false) SaveStatus saveStatus) {

        List<LogBaseListResultDTO> result = logBookService.getLogBooksByYearAndStatus(year, saveStatus);
        return ApiResponse.success(result);
    }

    @GetMapping("/{logBaseInfoId}")
    @Operation(summary = "로그 상세조회", description = "특정 로그북의 상세 정보를 조회합니다.")
    public ApiResponse<List<LogBookDetailResultDTO>> getLogDetail
            (@PathVariable Long logBaseInfoId) {
        List<LogBookDetailResultDTO> resultDTOS = logBookService.getLogDetail(logBaseInfoId);
        return ApiResponse.success(resultDTOS);
    }

    @PostMapping("/{logBaseInfoId}")
    @Operation(summary = "세부 로그 생성", description = "특정 날짜에 해당하는 로그 세부 정보를 생성합니다.")
    public ApiResponse<LogDetailCreateResultDTO> createLogBook
            (@PathVariable Long logBaseInfoId,
             @RequestBody @Valid LogDetailCreateRequestDTO dto) {
        LogDetailCreateResultDTO result = logBookService.createLogDetail(dto, logBaseInfoId);
        return ApiResponse.success(result);
    }

}
