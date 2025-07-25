package com.divary.domain.logbook.dto.request;

import com.divary.domain.logbook.enums.CompanionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanionRequestDTO {
    @Schema(description = "동반자 이름", example = "김버디")
    private String companion;

    @Schema(description = "동반자 타입", example = "LEADER")
    private CompanionType type;
}
