package com.divary.domain.member.service;

import com.divary.common.util.EnumValidator;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    String additionalPath = "qualifications";

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



    @jakarta.transaction.Transactional
    public void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO) {
        Levels level = EnumValidator.validateEnum(Levels.class, requestDTO.getLevel().name());


        Member member = memberRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.setLevel(level);
    }

}
