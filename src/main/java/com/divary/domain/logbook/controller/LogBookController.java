package com.divary.domain.logbook.controller;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbook.dto.LogBookRequestDTO;
import com.divary.domain.logbook.dto.LogBookResponseDTO;
import com.divary.domain.logbook.service.LogBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Log API", description = "로그북 관련 API")
public class LogBookController {

    private final LogBookService logBookServiceImpl;

    @PostMapping
    @Operation(summary = "로그 생성", description = "다이빙 로그를 생성합니다.")
    public ApiResponse<LogBookResponseDTO.CreateResultDTO> createLog
            (@RequestBody @Valid LogBookRequestDTO.CreateDTO createDTO)
    {
        LogBookResponseDTO.CreateResultDTO responseDto = logBookServiceImpl.createLog(createDTO);
        return ApiResponse.success(responseDto);
    }

}
