package com.divary.domain.logbook.dto;

import com.divary.domain.logbook.enums.IconType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


public class LogBookResponseDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatResultDTO{
        String name;
        IconType iconType;
    }

}
