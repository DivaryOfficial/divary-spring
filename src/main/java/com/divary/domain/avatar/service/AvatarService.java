package com.divary.domain.avatar.service;

import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;


public interface AvatarService {
    public void saveAvatar(AvatarRequestDTO avatarRequestDTO);

    AvatarResponseDTO getAvatar();
}
