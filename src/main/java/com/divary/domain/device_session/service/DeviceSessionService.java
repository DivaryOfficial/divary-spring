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
    public void saveToken(Member member,String refreshToken,String deviceId,SocialType socialType){
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
            deviceSessionRepository.deleteByUser_IdAndDeviceId(userId, deviceId); //특정 기기에서 특정 유저 로그아웃
        }
        else{
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
    @Transactional
    public void upsertRefreshToken(Member member, String refreshToken, String deviceId, SocialType socialType) {
        // 1. memberId와 deviceId로 기존 세션이 있는지 조회
        DeviceSession session = deviceSessionRepository.findByUser_IdAndDeviceId(member.getId(), deviceId);

        if (session != null) {
            // 2. 기존 세션이 있다면(null이 아니라면), Refresh Token만 갱신 (Update)
            session.updateToken(refreshToken);
        } else {
            // 3. 기존 세션이 없다면(null이라면), 새로 생성 (Insert)
            session = DeviceSession.builder()
                    .user(member)
                    .refreshToken(refreshToken)
                    .deviceId(deviceId)
                    .socialType(socialType)
                    .build();
            deviceSessionRepository.save(session);
        }
    }
}
