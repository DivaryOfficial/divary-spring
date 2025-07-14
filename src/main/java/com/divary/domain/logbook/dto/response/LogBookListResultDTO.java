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
public class LogBookListResultDTO {

    private String name;

    private LocalDate date;

    private IconType iconType;

}
