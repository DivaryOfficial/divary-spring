package com.divary.domain.avatar.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.member.entity.Member;
import com.divary.domain.avatar.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Avatar extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member user;

    @Column(length = 20)
    @Builder.Default
    private String name = "버디";

    @Builder.Default
    @Column(nullable = false,name = "body_color")
    @Enumerated(EnumType.STRING)
    private BodyColor bodyColor = BodyColor.IVORY;

    @Column(nullable = true, name = "buble_text")
    private String bubbleText;


    @Column(nullable = true, name = "speechBubble")
    @Enumerated(EnumType.STRING)
    private SpeechBubble speechBubble;


    @Column(name = "cheek_color")
    @Enumerated(EnumType.STRING)
    private CheekColor cheekColor;


    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "mask")
    private Mask mask;


    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "regulator")
    private Regulator regulator;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "pin")
    private Pin pin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "tank")
    private Tank tank;


    @Enumerated(EnumType.STRING)
    @Column(nullable = true, name = "budy_pet")
    private BudyPet budyPet;

    @Column(nullable = true, name = "pet_rotation")
    private Double petRotation;

    @Column(nullable = true, name = "pet_width")
    private Double petWidth;

    @Column(nullable = true, name = "pet_height")
    private Double petHeight;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "theme")
    private Theme theme = Theme.CORAL_FOREST;
}
