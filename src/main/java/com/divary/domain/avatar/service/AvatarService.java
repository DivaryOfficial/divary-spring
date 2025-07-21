package com.divary.domain.avatar.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.entity.Avatar;


public interface AvatarService {
    void patchAvatar(AvatarRequestDTO avatarRequestDTO);

    AvatarResponseDTO getAvatar();

    void createDefaultAvatarForMember(Member member);
}
