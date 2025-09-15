package com.divary.domain.token.repository;

import com.divary.domain.token.entity.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceSessionRepository extends JpaRepository<DeviceSession, Long> {
    boolean existsByRefreshTokenAndDeviceId(String refreshToken, String deviceId);
    void deleteByRefreshToken(String refreshToken);
    void deleteByUser_Id(Long user_id);

    DeviceSession findByUser_IdAndDeviceId(Long userId, String deviceId);

    void deleteByDeviceId(String deviceId);
}
