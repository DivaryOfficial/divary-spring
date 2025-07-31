package com.divary.domain.logbase.logbook.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogDetailCreateResultDTO {

    @Schema(description = "로그북id")
    private Long logBookId;

    @Schema(description = "성공 메시지")
    private String message;

}
