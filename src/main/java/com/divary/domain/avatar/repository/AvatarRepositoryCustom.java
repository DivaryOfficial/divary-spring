package com.divary.domain.avatar.repository;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.avatar.entity.Avatar;

public interface AvatarRepositoryCustom {
    Avatar saveByMember(Member member, Avatar avatar);
}
