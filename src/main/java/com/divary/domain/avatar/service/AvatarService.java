package com.divary.domain.avatar.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.entity.Avatar;


public interface AvatarService {
    void upsertAvatar(Long userId, AvatarRequestDTO avatarRequestDTO);

    AvatarResponseDTO getAvatar(Long userId);

    void createDefaultAvatarForMember(Member member);
}
