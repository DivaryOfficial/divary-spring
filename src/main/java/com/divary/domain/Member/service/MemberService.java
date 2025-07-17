package com.divary.domain.Member.service;

import com.divary.domain.Member.entity.Member;

public interface MemberService {
    Member findMemberByEmail(String email);
    Member findById(Long id);
    Member saveMember(Member member);
}
