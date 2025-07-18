package com.divary.domain.logbook.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbook.dto.request.LogBookCreateRequestDTO;
import com.divary.domain.logbook.dto.response.LogBaseListResultDTO;
import com.divary.domain.logbook.dto.response.LogBookCreateResultDTO;
import com.divary.domain.logbook.dto.response.LogBookDetailResultDTO;
import com.divary.domain.logbook.enums.SaveStatus;
import com.divary.domain.logbook.service.LogBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Log API", description = "로그북 관련 API")
public class LogBookController {

    private final LogBookService logBookService;

    @PostMapping
    @Operation(summary = "로그 생성", description = "다이빙 로그를 생성합니다.")
    public ApiResponse<LogBookCreateResultDTO> createLog
            (@RequestBody @Valid LogBookCreateRequestDTO createDTO)
    {
        LogBookCreateResultDTO responseDto = logBookService.createLog(createDTO);
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

    @GetMapping("/date/{date}")
    @Operation(summary = "로그 상세조회", description = "특정 로그북의 상세 정보를 조회합니다.")
    public ApiResponse<List<LogBookDetailResultDTO>> getLogDetail
            (@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<LogBookDetailResultDTO> resultDTOS = logBookService.getLogDetail(date);
        return ApiResponse.success(resultDTOS);
    }

}
