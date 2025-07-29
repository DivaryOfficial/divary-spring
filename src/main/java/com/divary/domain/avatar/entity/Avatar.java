package com.divary.domain.avatar.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.member.entity.Member;
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

    @Builder.Default
    @Column(nullable = false,name = "body_color")
    @Enumerated(EnumType.STRING)
    private BodyColor bodyColor = BodyColor.IVORY;

    @Column(name = "buble_text")
    private String bubbleText;

    @Builder.Default
    @Column(nullable = false, name = "speechBubble")
    @Enumerated(EnumType.STRING)
    private SpeechBubble speechBubble = SpeechBubble.NONE;

    @Builder.Default
    @Column(nullable = false, name = "cheek_color")
    @Enumerated(EnumType.STRING)
    private CheekColor cheekColor = CheekColor.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Mask mask = Mask.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Regulator regulator = Regulator.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Pin pin = Pin.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Tank tank = Tank.NONE;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "budy_pet")
    private BudyPet budyPet = BudyPet.NONE;

    @Column(name = "pet_rotation")
    private Double petRotation;

    @Column(name = "pet_scale")
    private Double petScale;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.CORAL_FOREST;
}
