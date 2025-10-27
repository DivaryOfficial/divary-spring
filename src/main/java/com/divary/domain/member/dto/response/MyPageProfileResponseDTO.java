package com.divary.domain.member.dto.response;

import com.divary.domain.member.enums.Levels;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyPageProfileResponseDTO {

    @Schema(description = "멤버 id", example = "user1234")
    private String id;

    @Schema(description = "단체명", example = "PADI")
    private String memberGroup;

    @Schema(description = "레벨", example = "오픈워터 다이버")
    private Levels level;

}
