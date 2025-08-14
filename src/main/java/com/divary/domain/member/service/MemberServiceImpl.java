package com.divary.domain.member.service;

import com.divary.common.util.EnumValidator;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.member.dto.response.MyPageImageResponseDTO;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    String additionalPath = "qualifications";

    @Override
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(()-> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
    }

    @Override
    @Cacheable(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#id")
    public Member findById(Long id) {
        return  memberRepository.findById(id).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#member.id", condition = "#member.id != null")
    public Member saveMember(Member member) {
        return memberRepository.save(member);
    }



    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#userId")
    public void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO) {
        Levels level = EnumValidator.validateEnum(Levels.class, requestDTO.getLevel().name());


        Member member = memberRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.setLevel(level);
    }

    @Override
    public MyPageImageResponseDTO uploadLicense(MultipartFile image, Long userId) {
        String uploadPath = "users/" + userId + "/license/";

        ImageUploadRequest request = ImageUploadRequest.builder()
                .file(image)
                .uploadPath(uploadPath)
                .build();

        String fileUrl = imageService.uploadImage(request).getFileUrl();


        return new MyPageImageResponseDTO(fileUrl);
    }

}
