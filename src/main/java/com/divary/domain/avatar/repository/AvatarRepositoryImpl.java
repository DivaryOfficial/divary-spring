package com.divary.domain.avatar.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.entity.Avatar;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AvatarRepositoryImpl implements AvatarRepositoryCustom{
    private final AvatarRepository avatarRepository;

    @Override
    public Avatar saveByMember(Member member, Avatar updatedAvatar) {
        Avatar avatar = avatarRepository.findByUser(member).orElseThrow(() -> new BusinessException(ErrorCode.AVATAR_NOT_FOUND));
        return null;
    }
}
