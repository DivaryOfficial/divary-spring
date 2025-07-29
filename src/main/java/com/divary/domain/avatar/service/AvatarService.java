package com.divary.domain.avatar.service;

import com.divary.domain.member.entity.Member;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;


public interface AvatarService {
    void patchAvatar(Long userId, AvatarRequestDTO avatarRequestDTO);

    AvatarResponseDTO getAvatar(Long userId);

    void createDefaultAvatarForMember(Member member);
}
