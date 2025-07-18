package com.divary.domain.avatar.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.enums.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Avatar extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private Member user;

    @Column(length = 20)
    private String name;

    @Column(nullable = false, name = "body_color")
    @Enumerated(EnumType.STRING)
    private BodyColor bodyColor;

    @Column(nullable = false, name = "eye_color")
    @Enumerated(EnumType.STRING)
    private SpeechBubble speechBubble;

    @Column(nullable = false, name = "cheek_color")
    @Enumerated(EnumType.STRING)
    private CheekColor cheekColor;

    @Enumerated(EnumType.STRING)
    private Mask mask;

    @Enumerated(EnumType.STRING)
    private Regulator regulator;

    @Enumerated(EnumType.STRING)
    private Pin pin;

    @Enumerated(EnumType.STRING)
    private Tank tank;

    @Enumerated(EnumType.STRING)
    @Column(name = "budy_pet")
    private BudyPet budyPet;

    @Enumerated(EnumType.STRING)
    private Theme theme;
}
