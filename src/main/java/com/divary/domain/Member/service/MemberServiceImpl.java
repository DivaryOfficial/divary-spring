package com.divary.domain.Member.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    @Override
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("Email not found"));
    }

    @Override
    public Member findById(Long id) {
        return  memberRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Member id not found"));
    }

    @Override
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}
