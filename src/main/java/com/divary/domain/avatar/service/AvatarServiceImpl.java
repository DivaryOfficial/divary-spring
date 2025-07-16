package com.divary.domain.avatar.service;

import com.divary.domain.Member.entity.Member;
import com.divary.domain.Member.service.MemberService;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.entity.Avatar;
import com.divary.domain.avatar.repository.AvatarRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AvatarServiceImpl implements AvatarService {
    private final AvatarRepository avatarRepository;
    private final MemberService memberService;

    @Override
    public void saveAvatar(AvatarRequestDTO avatarRequestDTO) {

        Member user = memberService.findById(1L); //임시

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

    }

    @Override
    public AvatarResponseDTO getAvatar(){
        Member user = memberService.findById(1L);

        Avatar avatar = avatarRepository.findByUser(user).orElseThrow(()-> new BusinessException(ErrorCode.AVATAR_NOT_FOUND)); //로그인  merge되면 MemberNotFound로 변경
        return AvatarResponseDTO.builder()
                .name(avatar.getName())
                .accessory(avatar.getAccessory())
                .bodyColor(avatar.getBodyColor())
                .budyPet(avatar.getBudyPet())
                .cheekColor(avatar.getCheekColor())
                .eyeColor(avatar.getEyeColor())
                .eyelash(avatar.getEyelash())
                .mask(avatar.getMask())
                .pin(avatar.getPin())
                .regulator(avatar.getRegulator())
                .theme(avatar.getTheme())
                .build();
    }
}
