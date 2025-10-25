package com.divary.domain.member.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.member.enums.Levels;
import com.divary.domain.member.enums.Role;
import com.divary.common.enums.SocialType;
import com.divary.domain.member.enums.Status;
import jakarta.annotation.Nullable;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {


    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @Enumerated(EnumType.STRING)
    private Levels level;

    @Enumerated(EnumType.STRING)
    @NotNull
    Status status = Status.ACTIVE;  // 사용자 상태

    @Column
    private String group;

    private LocalDateTime deactivatedAt; //비활성화 된 시간과 날짜

    @Version
    private Long version; //버전을통해 레이스 컨디션 해결


    // 탈퇴 요청 처리
    public void requestDeletion() {
        this.status = Status.DEACTIVATED;
        this.deactivatedAt = LocalDateTime.now();
    }

    // 탈퇴 요청 취소 (계정 복구)
    public void cancelDeletion() {
        this.status = Status.ACTIVE;
        this.deactivatedAt = null;
    }
    
}
