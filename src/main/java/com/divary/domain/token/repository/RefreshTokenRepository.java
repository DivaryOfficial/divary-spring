package com.divary.domain.token.repository;

import com.divary.domain.token.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    boolean existsByRefreshTokenAndDeviceId(String refreshToken, String deviceId);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUser_Id(Long user_id);

    RefreshToken findByUser_IdAndDeviceId(Long userId, String deviceId);

    void deleteByDeviceId(String deviceId);
}
