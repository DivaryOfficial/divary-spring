package com.divary.domain.Member.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.Member.enums.Level;
import com.divary.domain.Member.enums.Role;
import com.divary.common.enums.SocialType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {


    @NotNull
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private SocialType socialType;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @Enumerated(EnumType.STRING)
    private Level level;
}
