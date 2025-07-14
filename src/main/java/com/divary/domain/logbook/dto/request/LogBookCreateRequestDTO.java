package com.divary.domain.logbook.dto.request;

import com.divary.domain.logbook.enums.IconType;
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
public class LogBookCreateRequestDTO {

    @NotNull
    private Long memberId;

    @NotNull
    private IconType iconType;

    @NotBlank
    private String name;

    @NotNull
    private LocalDate date;
}