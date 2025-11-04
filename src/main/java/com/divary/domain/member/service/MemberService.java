package com.divary.domain.member.service;

import com.divary.common.enums.SocialType;
import com.divary.domain.member.dto.requestDTO.MyPageGroupRequestDTO;
import com.divary.domain.member.dto.response.MyPageImageResponseDTO;
import com.divary.domain.member.dto.response.MyPageProfileResponseDTO;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.global.oauth.dto.response.DeactivateResponse;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {
    Member findMemberByEmail(String email);
    Member findById(Long id);
    Member saveMember(Member member);
    void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO);
    MyPageImageResponseDTO uploadLicense(MultipartFile image, Long userId);
    DeactivateResponse requestToDeleteMember(Long memberId);
    void cancelDeleteMember(Long memberId);
    Member findOrCreateMemberBySocialId(String socialId, SocialType socialType, String email);

    void updateGroup(Long userId, MyPageGroupRequestDTO requestDTO);

    MyPageProfileResponseDTO getMemberProfile(Long userId);

    MyPageImageResponseDTO getLicenseImage(Long userId);
}
