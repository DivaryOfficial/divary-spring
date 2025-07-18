package com.divary.domain.logbook.dto.response;

import com.divary.domain.logbook.enums.IconType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogBaseListResultDTO {

    @Schema(description = "로그 제목", example = "해양일지")
    private String name;

    @Schema(description = "날짜", example = "2022-01-23")
    private LocalDate date;

    @Schema(description = "아이콘 타입", example = "CLOWNFISH")
    private IconType iconType;

}
