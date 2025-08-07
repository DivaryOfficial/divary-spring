package com.divary.domain.token.service;

import com.divary.domain.token.entity.RefreshToken;
import com.divary.domain.token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void updateRefreshToken(Long userId, String deviceId, String newRefreshToken) {
        RefreshToken originalRefresh = refreshTokenRepository.findByUser_IdAndDeviceId(userId, deviceId);
        originalRefresh.setRefreshToken(newRefreshToken);
    }
}
