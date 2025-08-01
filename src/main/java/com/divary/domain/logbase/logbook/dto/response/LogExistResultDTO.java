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
public class LogExistResultDTO {

    @Schema(description = "로그북베이스 존재 여부")
    private boolean exists;

    @Schema(description = "로그북베이스 id")
    private Long logBaseInfoId;

}
