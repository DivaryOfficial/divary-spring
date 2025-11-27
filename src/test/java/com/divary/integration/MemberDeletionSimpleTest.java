package com.divary.integration;

import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.repository.AvatarRepository;
import com.divary.domain.device_session.entity.DeviceSession;
import com.divary.domain.device_session.repository.DeviceSessionRepository;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.enums.Status;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.common.enums.SocialType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberDeletionSimpleTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private DeviceSessionRepository deviceSessionRepository;

    @Test
    @DisplayName("단순한 방법: @Transactional만으로 Member 삭제 테스트")
    void testSimpleMemberDeletion() {
        // Given: Member 생성
        Member member = Member.builder()
                .email("simple@test.com")
                .socialId("simple-social-id")
                .socialType(SocialType.APPLE)
                .role(Role.USER)
                .level(Levels.OPEN_WATER_DIVER)
                .status(Status.ACTIVE)
                .build();
        member = memberRepository.save(member);

        // Avatar 생성
        Avatar avatar = Avatar.builder()
                .user(member)
                .name("심플 버디")
                .build();
        avatar = avatarRepository.save(avatar);

        // DeviceSession 생성
        DeviceSession session = DeviceSession.builder()
                .user(member)
                .refreshToken("simple-token")
                .socialType(SocialType.APPLE)
                .deviceId("simple-device")
                .build();
        session = deviceSessionRepository.save(session);

        Long memberId = member.getId();
        Long avatarId = avatar.getId();
        Long sessionId = session.getId();

        // 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(avatarRepository.existsById(avatarId));
        assertTrue(deviceSessionRepository.existsById(sessionId));

        // When: Member를 간단히 삭제 (flush, clear 없이)
        memberRepository.delete(member);

        // Then: 모든 연관 데이터가 삭제되는지 확인
        assertFalse(memberRepository.existsById(memberId));
        assertFalse(avatarRepository.existsById(avatarId));
        assertFalse(deviceSessionRepository.existsById(sessionId));
    }
}
