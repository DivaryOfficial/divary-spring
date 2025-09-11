package com.divary.domain.token.service;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import com.divary.domain.token.entity.RefreshToken;
import com.divary.domain.token.repository.RefreshTokenRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.exception.GlobalExceptionHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void updateRefreshToken(Long userId, String deviceId, String newRefreshToken) {
        RefreshToken originalRefresh = refreshTokenRepository.findByUser_IdAndDeviceId(userId, deviceId);
        originalRefresh.updateToken(newRefreshToken);
    }
    public void saveToken(Member member,String accessToken,String refreshToken,String deviceId,SocialType socialType){
        refreshTokenRepository.save(RefreshToken.builder()
                .user(member)
                .deviceId(deviceId)
                .socialType(socialType)
                .refreshToken(refreshToken)
                .build());
    }
    public void removeRefreshToken(String deviceId, Long userId){
        if (userId != null && deviceId == null) {
            refreshTokenRepository.deleteByUser_Id(userId); //내 모든 기기에서 로그아웃
        }else if (userId != null && deviceId != null) {
            refreshTokenRepository.deleteByDeviceId(deviceId); //특정 기기에서 로그아웃
        }
        else{
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
