package com.divary.domain.mypage.service;

import com.divary.common.util.EnumValidator;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;
import com.divary.domain.member.service.MemberService;
import com.divary.domain.mypage.dto.requestDTO.MyPageImageRequestDTO;
import com.divary.domain.mypage.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.mypage.dto.response.MyPageImageResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MyPageService {
    private final MemberService memberService;
    private final ImageService imageService;
    String additionalPath = "qualifications";


    @Transactional
    public void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO) {
        Levels level = EnumValidator.validateEnum(Levels.class, requestDTO.getLevel().name());


        Member member = memberService.findById(userId);
        member.setLevel(level);
    }

    public MyPageImageResponseDTO uploadImage(ImageType type, MyPageImageRequestDTO requestDTO, Long userId) {

        ImageResponse imageResponse = imageService.uploadImageByType(type, requestDTO.getImage(), userId, additionalPath);

        return new MyPageImageResponseDTO(imageResponse.getFileUrl());
    }
}
