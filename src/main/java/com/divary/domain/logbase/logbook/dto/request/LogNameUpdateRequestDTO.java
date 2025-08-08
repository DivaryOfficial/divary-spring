package com.divary.domain.logbase.logbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogNameUpdateRequestDTO {
    @Schema(description = "수정할 로그 이름", example = "해양일지")
    @NotBlank
    private String name;
}
