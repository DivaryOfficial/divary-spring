package com.divary.domain.logbase.logbook.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.logbase.logbook.enums.CompanionType;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "동행자 정보")
public class Companion extends BaseEntity {


    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "logbook_id")
    private LogBook logBook;

    @Column(name = "companion_name", length = 50)
    @Schema(description = "동행자 이름", example = "김버디")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "companion_type")
    @Schema(description = "동행자 역할", example = "BUDDY")
    private CompanionType type;
}

