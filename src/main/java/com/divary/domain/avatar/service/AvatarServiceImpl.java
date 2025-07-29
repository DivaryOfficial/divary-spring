package com.divary.domain.avatar.service;

import com.divary.domain.member.entity.Member;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.avatar.dto.AvatarRequestDTO;
import com.divary.domain.avatar.dto.AvatarResponseDTO;
import com.divary.domain.avatar.dto.BuddyPetInfoDTO;
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
    public void upsertAvatar(Long userId, AvatarRequestDTO avatarRequestDTO) {

        Member user = memberService.findById(userId);
        Avatar avatar = avatarRepository.findByUser(user)
                .orElse(Avatar.builder()
                        .user(user)
                        .build());

        avatar.setName(avatarRequestDTO.getName());
        avatar.setTank(avatarRequestDTO.getTank());
        avatar.setBodyColor(avatarRequestDTO.getBodyColor());

        avatar.setBudyPet(avatarRequestDTO.getBuddyPetInfo().getBudyPet());
        avatar.setPetRotation(avatarRequestDTO.getBuddyPetInfo().getRotation());
        avatar.setPetScale(avatarRequestDTO.getBuddyPetInfo().getScale());

        avatar.setBubbleText(avatarRequestDTO.getBubbleText());
        avatar.setCheekColor(avatarRequestDTO.getCheekColor());
        avatar.setSpeechBubble(avatarRequestDTO.getSpeechBubble());
        avatar.setMask(avatarRequestDTO.getMask());
        avatar.setPin(avatarRequestDTO.getPin());
        avatar.setRegulator(avatarRequestDTO.getRegulator());
        avatar.setTheme(avatarRequestDTO.getTheme());

        avatarRepository.save(avatar);

        avatarRepository.save(avatar);

    }

    @Override
    public AvatarResponseDTO getAvatar(Long userId){
        Member user = memberService.findById(userId);

        Avatar avatar = avatarRepository.findByUser(user).orElseThrow(()-> new BusinessException(ErrorCode.AVATAR_NOT_FOUND)); //로그인  merge되면 MemberNotFound로 변경

        BuddyPetInfoDTO buddyPetInfo = BuddyPetInfoDTO.builder()
                .budyPet(avatar.getBudyPet())
                .rotation(avatar.getPetRotation())
                .scale(avatar.getPetScale())
                .build();

        return AvatarResponseDTO.builder()
                .name(avatar.getName())
                .tank(avatar.getTank())
                .bodyColor(avatar.getBodyColor())
                .budyPet(avatar.getBudyPet())
                .bubbleText(avatar.getBubbleText())
                .buddyPetInfo(buddyPetInfo)
                .cheekColor(avatar.getCheekColor())
                .speechBubble(avatar.getSpeechBubble())
                .mask(avatar.getMask())
                .pin(avatar.getPin())
                .regulator(avatar.getRegulator())
                .theme(avatar.getTheme())
                .build();
    }

    public void createDefaultAvatarForMember(Member member) {
        Avatar avatar = Avatar.builder()
                .user(member)
                .name("")
                .build();

        avatarRepository.save(avatar);
    }
}
