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

        Avatar avatar;

        avatar = avatarRepository.findByUserId(userId);

        if (avatar == null) {
            Member user = memberService.findById(userId);
            avatar = Avatar.builder()
                    .user(user)
                    .build();
        }


        avatar.setName(avatarRequestDTO.getName());
        avatar.setTank(avatarRequestDTO.getTank());
        avatar.setBodyColor(avatarRequestDTO.getBodyColor());

        if (avatarRequestDTO.getBuddyPetInfo() != null) {
            var petInfo = avatarRequestDTO.getBuddyPetInfo();
            avatar.setBudyPet(petInfo.getBudyPet());
            avatar.setPetRotation(petInfo.getRotation());

            if (petInfo.getOffset() != null) {
                avatar.setPetHeight(petInfo.getOffset().getHeight());
                avatar.setPetWidth(petInfo.getOffset().getWidth());
            }
        }

        avatar.setBubbleText(avatarRequestDTO.getBubbleText());
        avatar.setCheekColor(avatarRequestDTO.getCheekColor());
        avatar.setSpeechBubble(avatarRequestDTO.getSpeechBubble());
        avatar.setMask(avatarRequestDTO.getMask());
        avatar.setPin(avatarRequestDTO.getPin());
        avatar.setRegulator(avatarRequestDTO.getRegulator());
        avatar.setTheme(avatarRequestDTO.getTheme());

        avatarRepository.save(avatar);


    }

    @Override
    public AvatarResponseDTO getAvatar(Long userId){

        Avatar avatar = avatarRepository.findByUserId(userId);

        if (avatar == null) {
            return AvatarResponseDTO.builder().build(); //처음 아바타 생성전 조회시 그냥 빈 avatar 반환
        }

        BuddyPetInfoDTO buddyPetInfo = BuddyPetInfoDTO.builder()
                .budyPet(avatar.getBudyPet())
                .rotation(avatar.getPetRotation())
                .offset(BuddyPetInfoDTO.Offset.builder()
                        .width(avatar.getPetWidth())
                        .height(avatar.getPetHeight())
                        .build())
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
}
