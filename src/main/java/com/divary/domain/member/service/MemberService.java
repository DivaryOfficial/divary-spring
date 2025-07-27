package com.divary.domain.member.service;

import com.divary.domain.member.entity.Member;

public interface MemberService {
    Member findMemberByEmail(String email);
    Member findById(Long id);
    Member saveMember(Member member);
}
