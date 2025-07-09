package com.divary.domain.encyclopedia.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도감 생물 특이사항 응답 DTO")
public class SignificantResponse {
    private String toxicity;
    private String strategy;
    private String observeTip;
    private String otherFeature;
}
