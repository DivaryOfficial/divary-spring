package com.divary.domain.avatar.service;

import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.entity.Avatar;
import lombok.Builder;
import lombok.Getter;
import org.springframework.stereotype.Service;


public interface AvatarService {
    public Avatar saveAvatar(AvatarRequestDTO avatarRequestDTO);
}
