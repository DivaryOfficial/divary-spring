package com.divary.domain.member.service;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;

public interface MemberService {
    Member findMemberByEmail(String email);
    Member findById(Long id);
    Member saveMember(Member member);
    void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO);
}
