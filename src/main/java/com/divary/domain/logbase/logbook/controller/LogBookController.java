package com.divary.domain.logbase.logbook.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbase.logbook.dto.request.LogBaseCreateRequestDTO;
import com.divary.domain.logbase.logbook.dto.request.LogDetailPutRequestDTO;
import com.divary.domain.logbase.logbook.dto.request.LogNameUpdateRequestDTO;
import com.divary.domain.logbase.logbook.dto.response.*;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.logbase.logbook.service.LogBookService;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.config.SwaggerConfig.ApiSuccessResponse;
import com.divary.global.config.security.CustomUserPrincipal;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Log API", description = "로그북 관련 API")
public class LogBookController {

    private final LogBookService logBookService;

    @PostMapping
    @Operation(summary = "초기 로그 생성", description = "다이빙 로그를 생성합니다.")
    @ApiSuccessResponse(dataType = LogBaseCreateResultDTO.class)
    @ApiErrorExamples(value = {ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<LogBaseCreateResultDTO> createLogBase
            (@RequestBody @Valid LogBaseCreateRequestDTO createDTO,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal)
    {
        Long userId = userPrincipal.getId();
        LogBaseCreateResultDTO responseDto = logBookService.createLogBase(createDTO, userId);
        return ApiResponse.success(responseDto);
    }

    @GetMapping
    @ApiSuccessResponse(dataType = LogBaseListResultDTO.class)
    @ApiErrorExamples(value = {ErrorCode.AUTHENTICATION_REQUIRED})
    @Operation(summary = "로그 리스트 조회", description = "연도와 저장 상태에 따라 로그북 리스트를 조회합니다.")
    public ApiResponse<List<LogBaseListResultDTO>> getLogsByYearAndStatus(
            @RequestParam int year,
            @RequestParam(required = false) SaveStatus saveStatus,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        Long userId = userPrincipal.getId();
        List<LogBaseListResultDTO> result = logBookService.getLogBooksByYearAndStatus(year, saveStatus, userId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{logBaseInfoId}")
    @ApiSuccessResponse(dataType = LogBookDetailResultDTO.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_NOT_FOUND, ErrorCode.LOG_BASE_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    @Operation(summary = "로그 상세조회", description = "특정 로그북의 상세 정보를 조회합니다.")
    public ApiResponse<List<LogBookDetailResultDTO>> getLogDetail
            (@PathVariable Long logBaseInfoId) {
        List<LogBookDetailResultDTO> resultDTOS = logBookService.getLogDetail(logBaseInfoId);
        return ApiResponse.success(resultDTOS);
    }

    @PostMapping("/{logBaseInfoId}")
    @ApiSuccessResponse(dataType = LogDetailCreateResultDTO.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_ACCESS_DENIED, ErrorCode.LOG_BASE_NOT_FOUND,ErrorCode.LOG_LIMIT_EXCEEDED, ErrorCode.AUTHENTICATION_REQUIRED})
    @Operation(summary = "비어있는 세부 로그북 생성", description = "특정 날짜에 해당하는, 내용 없는 기본 로그북을 생성합니다.")
    public ApiResponse<LogDetailCreateResultDTO> createLogDetail
            (@PathVariable Long logBaseInfoId,
             @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Long userId = userPrincipal.getId();
        LogDetailCreateResultDTO result = logBookService.createLogDetail(logBaseInfoId, userId);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{logBaseInfoId}")
    @ApiSuccessResponse(dataType = void.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_ACCESS_DENIED, ErrorCode.LOG_BASE_NOT_FOUND,ErrorCode.LOG_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    @Operation(summary = "로그 삭제", description = "지정한 다이빙 로그를 삭제합니다.")
    public ApiResponse<Void> deleteLogBase
            (@PathVariable @Valid Long logBaseInfoId,
             @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {
        Long userId= userPrincipal.getId();
        logBookService.deleteLog(logBaseInfoId, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{logBookId}")
    @Operation(summary = "로그 전체 수정", description = "다이빙 로그 세부 정보를 전체 수정합니다.")
    @ApiSuccessResponse(dataType = LogDetailPutResultDTO.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_ACCESS_DENIED, ErrorCode.LOG_NOT_FOUND, ErrorCode.LOG_BASE_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<LogDetailPutResultDTO> updateLogDetail(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            @PathVariable Long logBookId,
            @RequestBody @Valid LogDetailPutRequestDTO dto) {

        LogDetailPutResultDTO result = logBookService.updateLogBook(userPrincipal.getId(), logBookId, dto);
        return ApiResponse.success(result);
    }

    @PatchMapping("/{logBaseInfoId}")
    @Operation(summary = "로그북 이름 변경", description = "로그북의 이름을 변경합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_ACCESS_DENIED, ErrorCode.LOG_BASE_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<Void> updateLogBaseName(
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal,
            @PathVariable Long logBaseInfoId,
            @RequestBody @Valid LogNameUpdateRequestDTO dto){

        Long userId = userPrincipal.getId();
        logBookService.updateLogName(logBaseInfoId, userId, dto.getName());
        return ApiResponse.success(null);
    }

    @GetMapping("/exists")
    @Operation(summary = "특정 날짜 로그베이스 존재 여부", description = "해당 날짜에 로그가 존재하는지 확인합니다.")
    @ApiSuccessResponse(dataType = Void.class)
    @ApiErrorExamples(value = {ErrorCode.LOG_ACCESS_DENIED, ErrorCode.LOG_BASE_NOT_FOUND, ErrorCode.AUTHENTICATION_REQUIRED})
    public ApiResponse<LogExistResultDTO> checkLogExists(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal CustomUserPrincipal userPrincipal) {

        Long userId = userPrincipal.getId();

        LogExistResultDTO response = logBookService.checkLogExists(date, userId);
        return ApiResponse.success(response);
    }

}
