package com.divary.domain.member.facade;

import com.divary.domain.logbase.logbook.service.LogBookService;
import com.divary.domain.member.dto.response.MyPageProfileResponseDTO;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.service.MemberService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * MyPage Facade
 * 여러 도메인 서비스를 조합하여 마이페이지 관련 비즈니스 로직을 처리합니다.
 *
 * 목적:
 * 1. MemberService와 LogBookService 간의 순환참조 제거
 * 2. 여러 도메인의 데이터를 조합하는 책임 분리
 * 3. 향후 소셜 기능(친구 등) 추가 시 확장성 확보
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacade {

    private final MemberService memberService;
    private final LogBookService logBookService;

    /**
     * 마이페이지 프로필 정보 조회
     * Member 정보와 LogBook 누적 횟수를 조합하여 반환
     *
     * @param userId 사용자 ID
     * @return 마이페이지 프로필 정보
     */
    public MyPageProfileResponseDTO getMemberProfile(Long userId) {
        // 1. Member 도메인 데이터 조회
        Member member = memberService.findById(userId);

        // 2. LogBook 도메인 데이터 조회
        Integer accumulation = logBookService.getAccumulationById(userId);

        // 3. 비즈니스 로직: 이메일에서 ID 추출
        String memberIdByEmail = member.getEmail().split("@")[0];

        // 4. DTO 조합 및 반환
        return MyPageProfileResponseDTO.builder()
                .memberGroup(member.getMemberGroup())
                .level(member.getLevel())
                .id(memberIdByEmail)
                .accumulations(accumulation)
                .build();
    }
}
