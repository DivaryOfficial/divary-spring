package com.divary.domain.logbase.logbook.dto.response;

import com.divary.domain.logbase.logbook.entity.Companion;
import com.divary.domain.logbase.logbook.enums.IconType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanionResultDTO {

    private String companion;
    private String type;

    public static CompanionResultDTO from(Companion entity) {
        return CompanionResultDTO.builder()
                .companion(entity.getName())
                .type(entity.getType().name())
                .build();
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LogBaseListResultDTO {

        @Schema(description = "로그 제목", example = "해양일지")
        private String name;

        @Schema(description = "날짜", example = "2022-01-23")
        private LocalDate date;

        @Schema(description = "아이콘 타입", example = "CLOWNFISH")
        private IconType iconType;

        @Schema(description = "베이스로그 id")
        private Long LogBaseInfoId;

    }
}
