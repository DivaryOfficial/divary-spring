package com.divary.domain.logbook.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogDetailPutResultDTO {

    private Long logBookId;

    private String message;
}

