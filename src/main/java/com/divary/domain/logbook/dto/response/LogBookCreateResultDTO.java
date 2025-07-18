package com.divary.domain.logbook.dto.response;

import com.divary.domain.logbook.enums.IconType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogBookCreateResultDTO {

    private String name;

    private IconType iconType;

    private LocalDate date;

    private int accumulation;

}
