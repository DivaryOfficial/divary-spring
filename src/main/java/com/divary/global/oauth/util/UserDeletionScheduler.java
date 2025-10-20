package com.divary.global.oauth.util;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Status;
import com.divary.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserDeletionScheduler {

    // 유예 기간 (예: 7일)
    @Value("${jobs.user-deletion.grace-period-days}")
    private int gracePeriodDays;

    private final MemberRepository memberRepository;

    public UserDeletionScheduler(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 매일 밤 12시에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "${jobs.user-deletion.cron}")
    @Transactional
    public void cleanupDeactivatedUsers() {
        System.out.println("탈퇴 유예 기간이 지난 사용자 삭제 작업을 시작합니다...");

        // 유예 기간이 지난 탈퇴 요청 사용자 조회
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(gracePeriodDays);

        List<Member> usersToDelete = memberRepository.findByStatusAndDeactivatedAtBefore(
                Status.DEACTIVATED,
                cutoffDate
        );

        // 실제 데이터 영구 삭제
        if (!usersToDelete.isEmpty()) {
            memberRepository.deleteAll(usersToDelete);
            System.out.println(usersToDelete.size() + "명의 사용자 정보가 영구 삭제되었습니다.");
        } else {
            System.out.println("삭제할 사용자가 없습니다.");
        }
    }
}
