package com.divary.domain.member.service;

import com.divary.common.util.EnumValidator;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.member.dto.response.MyPageImageResponseDTO;
import com.divary.domain.member.enums.Role;
import com.divary.domain.member.enums.Status;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import com.divary.global.oauth.dto.response.DeactivateResponse;
import com.divary.global.redis.service.TokenBlackListService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.divary.domain.member.repository.MemberRepository;
import com.divary.domain.member.entity.Member;
import com.divary.domain.member.enums.Levels;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberServiceImpl implements MemberService {
    private final MemberRepository memberRepository;
    private final ImageService imageService;
    private final TokenBlackListService tokenBlackListService;

    @Value("${jobs.user-deletion.grace-period-days}")
    private int gracePeriodDays;

    @Override
    @Transactional(readOnly = true)
    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email).orElseThrow(()-> new BusinessException(ErrorCode.EMAIL_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
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

    @Override
    @Transactional
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#memberId")
    public DeactivateResponse requestToDeleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == Status.DEACTIVATED) {
            return new DeactivateResponse(member.getDeactivatedAt());
        }
        member.requestDeletion();

        LocalDateTime scheduledDeletionAt = member.getDeactivatedAt()
                .plusDays(gracePeriodDays);

        return new DeactivateResponse(scheduledDeletionAt);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#memberId")
    public void cancelDeleteMember(Long memberId) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // DEACTIVATED 상태일 때만 취소 가능
            if (member.getStatus() == Status.DEACTIVATED) {
                member.cancelDeletion();
            }
    }
    @Override
    @Transactional
    public Member findOrCreateMember(String email) {
        // 1. Optional을 사용하여 회원을 조회합니다.
        Optional<Member> optionalMember = memberRepository.findByEmail(email);

        // 2. 회원이 존재하면 그대로 반환하고, 존재하지 않으면 새로 생성하여 저장한 뒤 반환합니다.
        return optionalMember.orElseGet(() -> {
            Member newMember = Member.builder()
                    .email(email)
                    .status(Status.ACTIVE)
                    .role(Role.USER)
                    .build();
            return memberRepository.save(newMember);
        });
    }
}
