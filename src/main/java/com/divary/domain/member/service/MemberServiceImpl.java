package com.divary.domain.member.service;

import com.divary.common.enums.SocialType;
import com.divary.common.util.EnumValidator;
import com.divary.domain.image.dto.request.ImageUploadRequest;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.service.ImageService;
import com.divary.domain.member.dto.requestDTO.MyPageGroupRequestDTO;
import com.divary.domain.member.dto.requestDTO.MyPageLevelRequestDTO;
import com.divary.domain.member.dto.response.MyPageImageResponseDTO;
import com.divary.domain.member.dto.response.MyPageProfileResponseDTO;
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
import java.util.List;
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
    @Cacheable(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#id")
    public Member findById(Long id) {
        return  memberRepository.findById(id).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#member.id", condition = "#member.id != null")
    public Member saveMember(Member member) {
        // If member has an ID, it might be a detached entity from cache
        // Re-fetch from DB to get managed entity with proper version
        if (member.getId() != null) {
            Member managedMember = memberRepository.findById(member.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // Copy all fields from detached member to managed member
            managedMember.setEmail(member.getEmail());
            managedMember.setLevel(member.getLevel());
            managedMember.setRole(member.getRole());
            managedMember.setStatus(member.getStatus());
            managedMember.setDeactivatedAt(member.getDeactivatedAt());

            return managedMember; // JPA dirty checking will save this
        }

        // New entity - just save directly
        return memberRepository.save(member);
    }



    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#userId", beforeInvocation = false)
    public void updateLevel(Long userId, MyPageLevelRequestDTO requestDTO) {
        Levels level = EnumValidator.validateEnum(Levels.class, requestDTO.getLevel().name());

        Member member = memberRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.setLevel(level);
        // Cache evicted after successful update
    }

    @Override
    public MyPageImageResponseDTO uploadLicense(MultipartFile image, Long userId) {
        String uploadPath = "users/" + userId + "/license/";

        ImageUploadRequest request = ImageUploadRequest.builder()
                .file(image)
                .uploadPath(uploadPath)
                .build();

        imageService.uploadImage(request);

        // Pre-signed URL 생성
        List<ImageResponse> imageResponses = imageService.getImagesByPath(uploadPath);
        String fileUrl = imageResponses.getFirst().getFileUrl();

        return new MyPageImageResponseDTO(fileUrl);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#memberId", beforeInvocation = false)
    public DeactivateResponse requestToDeleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (member.getStatus() == Status.DEACTIVATED) {
            return new DeactivateResponse(member.getDeactivatedAt());
        }
        member.requestDeletion();

        LocalDateTime scheduledDeletionAt = member.getDeactivatedAt()
                .plusDays(gracePeriodDays);

        return new DeactivateResponse(scheduledDeletionAt);
        // Cache evicted after successful update
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#memberId", beforeInvocation = false)
    public void cancelDeleteMember(Long memberId) {
            Member member = memberRepository.findById(memberId).orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

            // DEACTIVATED 상태일 때만 취소 가능
            if (member.getStatus() == Status.DEACTIVATED) {
                member.cancelDeletion();
            }
            // Cache evicted after successful update
    }
    @Override
    @Transactional
    public Member findOrCreateMemberBySocialId(String socialId, SocialType socialType, String email) {
        // 1. socialId와 socialType으로 회원 조회
        Optional<Member> optionalMember = memberRepository.findBySocialIdAndSocialType(socialId, socialType);

        // 2. 회원이 존재하면 그대로 반환
        if (optionalMember.isPresent()) {
            return optionalMember.get();
        }

        // 3. 기존 회원 마이그레이션: email로 기존 회원 찾기
        if (email != null && !email.isEmpty()) {
            Optional<Member> existingMember = memberRepository.findByEmail(email);
            if (existingMember.isPresent()) {
                Member member = existingMember.get();

                // 3-1. 기존 회원의 socialId가 없으면 업데이트 (마이그레이션)
                if (member.getSocialId() == null) {
                    member.updateSocialInfo(socialId, socialType);
                    return member;  // JPA dirty checking으로 자동 저장
                }

                // 3-2. 이미 다른 소셜 타입으로 가입된 경우
                if (!member.getSocialType().equals(socialType)) {
                    throw new BusinessException(
                        ErrorCode.ALREADY_REGISTERED_WITH_DIFFERENT_SOCIAL,
                        "이 이메일은 이미 " + member.getSocialType() + " 계정으로 가입되어 있습니다. " +
                        member.getSocialType() + " 로그인을 사용해주세요."
                    );
                }

                // 3-3. 같은 소셜 타입인데 다른 socialId인 경우 (비정상 케이스)
                throw new BusinessException(ErrorCode.INVALID_TOKEN, "계정 정보가 일치하지 않습니다.");
            }
        }

        // 4. 새로운 회원 생성
        // 첫 로그인 시 email이 없으면 예외 발생
        if (email == null || email.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN,
                socialType + " 첫 로그인 시 이메일이 필요합니다.");
        }

        Member newMember = Member.builder()
                .email(email)
                .socialId(socialId)
                .socialType(socialType)
                .status(Status.ACTIVE)
                .role(Role.USER)
                .build();
        return memberRepository.save(newMember);
    }
    @Override
    @CacheEvict(cacheNames = com.divary.global.config.CacheConfig.CACHE_MEMBER_BY_ID, key = "#userId")
    public void updateGroup(Long userId, MyPageGroupRequestDTO requestDTO){
        String group = requestDTO.getMemberGroup();

        Member member = memberRepository.findById(userId).orElseThrow(()-> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateGroup(group);
    }


    @Override
    public MyPageImageResponseDTO getLicenseImage(Long userId){

        String uploadPath = "users/" + userId + "/license/";

        List<ImageResponse> imageResponses = imageService.getImagesByPath(uploadPath);

        String fileUrl = imageResponses.getFirst().getFileUrl();

        return new MyPageImageResponseDTO(fileUrl);
    }


}
