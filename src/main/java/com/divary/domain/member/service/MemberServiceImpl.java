package com.divary.domain.member.service;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
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
        return memberRepository.findByEmail(email).orElseThrow(()-> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
    }

    @Override
    public Member findById(Long id) {
        return  memberRepository.findById(id).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }
}
