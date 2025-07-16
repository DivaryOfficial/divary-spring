package com.divary.domain.avatar.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberService;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.repository.AvatarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {
    private final AvatarRepository avatarRepository;
    private final MemberService memberService;

    @Override
    public Avatar saveAvatar(AvatarRequestDTO avatarRequestDTO) {

        Member user = memberService.findById(1L);

        Avatar avatar = Avatar.builder()
                .name(avatarRequestDTO.getName())
                .accessory(avatarRequestDTO.getAccessory())
                .bodyColor(avatarRequestDTO.getBodyColor())
                .budyPet(avatarRequestDTO.getBudyPet())
                .cheekColor(avatarRequestDTO.getCheekColor())
                .eyeColor(avatarRequestDTO.getEyeColor())
                .eyelash(avatarRequestDTO.getEyelash())
                .mask(avatarRequestDTO.getMask())
                .pin(avatarRequestDTO.getPin())
                .regulator(avatarRequestDTO.getRegulator())
                .theme(avatarRequestDTO.getTheme())
                .build();
        avatarRepository.saveByMember(user, avatar);

        return avatar;
    }
}
