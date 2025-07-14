package com.divary.domain.logbook.dto.response;

import com.divary.domain.logbook.entity.Companion;
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
}
