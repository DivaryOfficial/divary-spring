package com.divary.domain.refresh.repository;

import com.divary.domain.refresh.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByRefreshToken(String token);
    void deleteByRefreshToken(String refreshToken);
}
