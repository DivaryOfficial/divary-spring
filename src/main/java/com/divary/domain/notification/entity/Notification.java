package com.divary.domain.notification.entity;

import com.divary.common.entity.BaseEntity;
import com.divary.domain.Member.entity.Member;
import com.divary.domain.notification.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private Member receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    private Boolean isRead = false;
}
