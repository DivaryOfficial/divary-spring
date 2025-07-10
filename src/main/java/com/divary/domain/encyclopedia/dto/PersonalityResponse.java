package com.divary.domain.encyclopedia.dto;

import com.divary.domain.encyclopedia.embedded.Personality;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "도감카드 성격 정보 응답")
public class PersonalityResponse {

    @Schema(description = "활동성", example = "움직임이 매우 느리고 조용함")
    private String activity;

    @Schema(description = "사회성", example = "작은 단독 생활, 무리 지어 다니지 않음")
    private String socialSkill;

    @Schema(description = "행동", example = "해양 바닥에 거의 머무르며 은신처 중심으로 생활")
    private String behavior;

    @Schema(description = "반응성", example = "위협 자극에 민감하지 않고 느긋한 편")
    private String reactivity;

    public static PersonalityResponse from(Personality personality) {
        return PersonalityResponse.builder()
                .activity(personality.getActivity())
                .socialSkill(personality.getSocialSkill())
                .behavior(personality.getBehavior())
                .reactivity(personality.getReactivity())
                .build();
    }
}
