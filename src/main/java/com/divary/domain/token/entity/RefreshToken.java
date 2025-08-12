package com.divary.domain.token.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
public class RefreshToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Member user;

    @Column(nullable = false)
    private String refreshToken;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SocialType socialType;

    @Column(nullable = false)
    private String deviceId;
}
