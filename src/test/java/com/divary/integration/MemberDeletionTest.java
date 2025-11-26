package com.divary.integration;

import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.repository.AvatarRepository;
import com.divary.domain.device_session.entity.DeviceSession;
import com.divary.domain.device_session.repository.DeviceSessionRepository;
import com.divary.domain.logbase.LogBaseInfo;
import com.divary.domain.logbase.LogBaseInfoRepository;
import com.divary.domain.logbase.logbook.enums.IconType;
import com.divary.domain.logbase.logbook.enums.SaveStatus;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.enums.Status;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.notification.entity.Notification;
import com.divary.domain.notification.enums.NotificationType;
import com.divary.domain.notification.repository.NotificationRepository;
import com.divary.common.enums.SocialType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MemberDeletionTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private LogBaseInfoRepository logBaseInfoRepository;

    @Autowired
    private DeviceSessionRepository deviceSessionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Member testMember;

    @BeforeEach
    void setUp() {
        // 테스트 회원 생성
        testMember = Member.builder()
                .email("test@example.com")
                .socialId("test-social-id")
                .socialType(SocialType.APPLE)
                .role(Role.USER)
                .level(Levels.OPEN_WATER_DIVER)
                .status(Status.ACTIVE)
                .build();
        testMember = memberRepository.save(testMember);
    }

    @Test
    @DisplayName("Member 삭제 시 Avatar도 함께 삭제되어야 함 (CascadeType.ALL)")
    void testMemberDeletionWithAvatar() {
        // Given: Avatar 생성
        Avatar avatar = Avatar.builder()
                .user(testMember)
                .name("테스트 버디")
                .build();
        avatar = avatarRepository.save(avatar);
        avatarRepository.flush();

        Long memberId = testMember.getId();
        Long avatarId = avatar.getId();

        // 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(avatarRepository.existsById(avatarId));

        // When: Member 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: Avatar도 함께 삭제되어야 함
        assertFalse(memberRepository.existsById(memberId));
        assertFalse(avatarRepository.existsById(avatarId));
    }

    @Test
    @DisplayName("Member 삭제 시 DeviceSession도 함께 삭제되어야 함 (@OnDelete CASCADE)")
    void testMemberDeletionWithDeviceSession() {
        // Given: DeviceSession 생성
        DeviceSession session = DeviceSession.builder()
                .user(testMember)
                .refreshToken("test-refresh-token")
                .socialType(SocialType.APPLE)
                .deviceId("test-device-id")
                .build();
        session = deviceSessionRepository.save(session);
        deviceSessionRepository.flush();

        Long memberId = testMember.getId();
        Long sessionId = session.getId();

        // 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(deviceSessionRepository.existsById(sessionId));

        // When: Member 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: DeviceSession도 함께 삭제되어야 함
        assertFalse(memberRepository.existsById(memberId));
        assertFalse(deviceSessionRepository.existsById(sessionId));
    }

    @Test
    @DisplayName("Member 삭제 시 Notification도 함께 삭제되어야 함 (@OnDelete CASCADE)")
    void testMemberDeletionWithNotification() {
        // Given: Notification 생성
        Notification notification = Notification.builder()
                .receiver(testMember)
                .type(NotificationType.SYSTEM)
                .message("테스트 알림")
                .isRead(false)
                .build();
        notification = notificationRepository.save(notification);
        notificationRepository.flush();

        Long memberId = testMember.getId();
        Long notificationId = notification.getId();

        // 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(notificationRepository.existsById(notificationId));

        // When: Member 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: Notification도 함께 삭제되어야 함
        assertFalse(memberRepository.existsById(memberId));
        assertFalse(notificationRepository.existsById(notificationId));
    }

    @Test
    @DisplayName("Member 삭제 시 LogBaseInfo도 함께 삭제되어야 함 (@OnDelete CASCADE)")
    void testMemberDeletionWithLogBaseInfo() {
        // Given: LogBaseInfo 생성
        LogBaseInfo logBaseInfo = LogBaseInfo.builder()
                .member(testMember)
                .name("테스트 로그")
                .iconType(IconType.CLOWNFISH)
                .date(LocalDate.now())
                .saveStatus(SaveStatus.COMPLETE)
                .build();
        logBaseInfo = logBaseInfoRepository.save(logBaseInfo);
        logBaseInfoRepository.flush();

        Long memberId = testMember.getId();
        Long logBaseInfoId = logBaseInfo.getId();

        // 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(logBaseInfoRepository.existsById(logBaseInfoId));

        // When: Member 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: LogBaseInfo도 함께 삭제되어야 함
        assertFalse(memberRepository.existsById(memberId));
        assertFalse(logBaseInfoRepository.existsById(logBaseInfoId));
    }

    @Test
    @DisplayName("Member 삭제 시 모든 연관 데이터가 함께 삭제되어야 함 (통합 테스트)")
    void testMemberDeletionWithAllRelatedData() {
        // Given: 모든 연관 엔티티 생성
        Avatar avatar = Avatar.builder()
                .user(testMember)
                .name("테스트 버디")
                .build();
        avatar = avatarRepository.save(avatar);

        DeviceSession session = DeviceSession.builder()
                .user(testMember)
                .refreshToken("test-refresh-token")
                .socialType(SocialType.APPLE)
                .deviceId("test-device-id")
                .build();
        session = deviceSessionRepository.save(session);

        Notification notification = Notification.builder()
                .receiver(testMember)
                .type(NotificationType.SYSTEM)
                .message("테스트 알림")
                .isRead(false)
                .build();
        notification = notificationRepository.save(notification);

        LogBaseInfo logBaseInfo = LogBaseInfo.builder()
                .member(testMember)
                .name("테스트 로그")
                .iconType(IconType.CLOWNFISH)
                .date(LocalDate.now())
                .saveStatus(SaveStatus.COMPLETE)
                .build();
        logBaseInfo = logBaseInfoRepository.save(logBaseInfo);

        // flush to ensure all data is saved
        avatarRepository.flush();
        deviceSessionRepository.flush();
        notificationRepository.flush();
        logBaseInfoRepository.flush();

        Long memberId = testMember.getId();
        Long avatarId = avatar.getId();
        Long sessionId = session.getId();
        Long notificationId = notification.getId();
        Long logBaseInfoId = logBaseInfo.getId();

        // 모든 데이터 저장 확인
        assertTrue(memberRepository.existsById(memberId));
        assertTrue(avatarRepository.existsById(avatarId));
        assertTrue(deviceSessionRepository.existsById(sessionId));
        assertTrue(notificationRepository.existsById(notificationId));
        assertTrue(logBaseInfoRepository.existsById(logBaseInfoId));

        // When: Member 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: 모든 연관 데이터가 함께 삭제되어야 함
        assertFalse(memberRepository.existsById(memberId), "Member가 삭제되어야 함");
        assertFalse(avatarRepository.existsById(avatarId), "Avatar가 삭제되어야 함");
        assertFalse(deviceSessionRepository.existsById(sessionId), "DeviceSession이 삭제되어야 함");
        assertFalse(notificationRepository.existsById(notificationId), "Notification이 삭제되어야 함");
        assertFalse(logBaseInfoRepository.existsById(logBaseInfoId), "LogBaseInfo가 삭제되어야 함");
    }

    @Test
    @DisplayName("탈퇴 요청 후 Member 삭제 시나리오")
    void testDeactivatedMemberDeletion() {
        // Given: 탈퇴 요청한 회원
        testMember.requestDeletion();
        testMember = memberRepository.save(testMember);

        // Avatar 생성
        Avatar avatar = Avatar.builder()
                .user(testMember)
                .name("테스트 버디")
                .build();
        avatar = avatarRepository.save(avatar);
        avatarRepository.flush();

        assertEquals(Status.DEACTIVATED, testMember.getStatus());
        assertNotNull(testMember.getDeactivatedAt());

        Long memberId = testMember.getId();

        // When: 탈퇴 기간이 지나서 삭제
        entityManager.flush();
        entityManager.clear();
        Member memberToDelete = memberRepository.findById(memberId).orElseThrow();
        entityManager.remove(memberToDelete);
        entityManager.flush();

        // Then: Member와 모든 연관 데이터 삭제됨
        assertFalse(memberRepository.existsById(memberId));
    }
}
