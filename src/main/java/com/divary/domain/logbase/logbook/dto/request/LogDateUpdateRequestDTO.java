package com.divary.domain.logbase.logbook.dto.request;

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
public class LogDateUpdateRequestDTO {

    @Schema(description = "날짜")
    private LocalDate date;
}
