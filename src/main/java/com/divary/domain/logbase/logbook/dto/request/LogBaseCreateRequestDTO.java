package com.divary.domain.logbase.logbook.dto.request;

import com.divary.domain.logbase.logbook.enums.IconType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogBaseCreateRequestDTO {


    @NotNull
    @Schema(description = "아이콘 타입", example = "CLOWNFISH")
    private IconType iconType;

    //size 제한은 추후에 정해지면 진행
    @NotBlank
    @Schema(description = "로그북 이름", example = "해양일지")
    private String name;

    @NotNull
    @Schema(description = "날짜", example = "2025-12-23")
    private LocalDate date;
}