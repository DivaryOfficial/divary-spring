package com.divary.domain.device_session.service;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.entity.Member;
import com.divary.domain.device_session.entity.DeviceSession;
import com.divary.domain.device_session.repository.DeviceSessionRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceSessionService {
    private final DeviceSessionRepository deviceSessionRepository;

    @Transactional
    public void updateRefreshToken(Long userId, String deviceId, String newRefreshToken) {
        DeviceSession originalRefresh = deviceSessionRepository.findByUser_IdAndDeviceId(userId, deviceId);
        originalRefresh.updateToken(newRefreshToken);
    }
    public void saveToken(Member member,String accessToken,String refreshToken,String deviceId,SocialType socialType){
        deviceSessionRepository.save(DeviceSession.builder()
                .user(member)
                .deviceId(deviceId)
                .socialType(socialType)
                .refreshToken(refreshToken)
                .build());
    }
    public void removeRefreshToken(String deviceId, Long userId){
        if (userId != null && deviceId == null) {
            deviceSessionRepository.deleteByUser_Id(userId); //내 모든 기기에서 로그아웃
        }else if (userId != null && deviceId != null) {
            deviceSessionRepository.deleteByDeviceId(deviceId); //특정 기기에서 로그아웃
        }
        else{
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
