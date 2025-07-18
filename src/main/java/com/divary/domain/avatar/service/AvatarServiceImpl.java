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
    public void patchAvatar(AvatarRequestDTO avatarRequestDTO) {

        Member user = memberService.findById(1L); //임시
        Avatar avatar = avatarRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.AVATAR_NOT_FOUND));

        if (avatarRequestDTO.getName() != null) {
            avatar.setName(avatarRequestDTO.getName());
        }
        if (avatarRequestDTO.getTank() != null) {
            avatar.setTank(avatarRequestDTO.getTank());
        }
        if (avatarRequestDTO.getBodyColor() != null) {
            avatar.setBodyColor(avatarRequestDTO.getBodyColor());
        }
        if (avatarRequestDTO.getBudyPet() != null) {
            avatar.setBudyPet(avatarRequestDTO.getBudyPet());
        }
        if (avatarRequestDTO.getCheekColor() != null) {
            avatar.setCheekColor(avatarRequestDTO.getCheekColor());
        }
        if (avatarRequestDTO.getSpeechBubble() != null) {
            avatar.setSpeechBubble(avatarRequestDTO.getSpeechBubble());
        }
        if (avatarRequestDTO.getMask() != null) {
            avatar.setMask(avatarRequestDTO.getMask());
        }
        if (avatarRequestDTO.getPin() != null) {
            avatar.setPin(avatarRequestDTO.getPin());
        }
        if (avatarRequestDTO.getRegulator() != null) {
            avatar.setRegulator(avatarRequestDTO.getRegulator());
        }
        if (avatarRequestDTO.getTheme() != null) {
            avatar.setTheme(avatarRequestDTO.getTheme());
        }

        avatarRepository.save(avatar);

    }

    @Override
    public AvatarResponseDTO getAvatar(){
        Member user = memberService.findById(1L);

        Avatar avatar = avatarRepository.findByUser(user).orElseThrow(()-> new BusinessException(ErrorCode.AVATAR_NOT_FOUND)); //로그인  merge되면 MemberNotFound로 변경
        return AvatarResponseDTO.builder()
                .name(avatar.getName())
                .tank(avatar.getTank())
                .bodyColor(avatar.getBodyColor())
                .budyPet(avatar.getBudyPet())
                .cheekColor(avatar.getCheekColor())
                .speechBubble(avatar.getSpeechBubble())
                .mask(avatar.getMask())
                .pin(avatar.getPin())
                .regulator(avatar.getRegulator())
                .theme(avatar.getTheme())
                .build();
    }
}
