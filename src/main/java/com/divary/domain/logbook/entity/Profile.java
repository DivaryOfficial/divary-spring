package com.divary.domain.logbook.entity;
import com.divary.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Schema(description = "다이빙 프로파일")
public class Profile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "프로파일 ID", example = "50")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "log_id")
    private LogBook logBook;

    @Column(name = "dive_time")
    @Schema(description = "총 다이빙 시간(분)", example = "45")
    private Integer diveTime;

    @Column(name = "max_depth")
    @Schema(description = "최대 수심(m)", example = "23")
    private Integer maxDepth;

    @Column(name = "avg_depth")
    @Schema(description = "평균 수심(m)", example = "15")
    private Integer avgDepth;

    @Column(name = "decompress_depth")
    @Schema(description = "감압정지 깊이(m)", example = "6")
    private Integer decompressDepth;

    @Column(name = "decompress_time")
    @Schema(description = "감압정지 시간(분)", example = "3")
    private Integer decompressTime;

    @Column(name = "start_pressure")
    @Schema(description = "시작 탱크 압력(bar)", example = "200")
    private Integer startPressure;

    @Column(name = "finish_pressure")
    @Schema(description = "종료 탱크 압력(bar)", example = "80")
    private Integer finishPressure;

    @Column(name = "consumption")
    @Schema(description = "기체 소비량(bar)", example = "120")
    private Integer consumption;
}