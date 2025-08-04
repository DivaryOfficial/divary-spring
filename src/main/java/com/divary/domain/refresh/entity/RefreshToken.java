package com.divary.domain.refresh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @Column(nullable = false)
    private String refreshToken;

}
