package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.dto.PersonalityResponse;
import com.divary.domain.encyclopedia.dto.SignificantResponse;
import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.enums.Type;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
import com.divary.domain.image.dto.response.ImageResponse;
import com.divary.domain.image.enums.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EncyclopediaCardService {

    private final EncyclopediaCardRepository encyclopediaCardRepository;
    private final ImageService imageService;

    private static Type convertDescriptionToEnum(String description) {
        return Arrays.stream(Type.values())
                .filter(type -> type.getDescription().equals(description))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TYPE_NOT_FOUND));
    }

    private static boolean isValidDescription(String description) {
        return Arrays.stream(Type.values())
                .anyMatch(t -> t.getDescription().equals(description));
    }

    @Transactional(readOnly = true)
    public List<EncyclopediaCardSummaryResponse> getCards(String description) {
        List<EncyclopediaCard> cards;
        if (description == null) {
            cards = encyclopediaCardRepository.findAll();
        } else {
            if (!isValidDescription(description)) {
                throw new BusinessException(ErrorCode.TYPE_NOT_FOUND);
            }
            Type typeEnum = convertDescriptionToEnum(description);
            cards = encyclopediaCardRepository.findAllByType(typeEnum);
        }

        // 모든 도감 프로필 (도감 이모티콘) 한 번에 조회
        List<ImageResponse> allDogamProfiles = imageService.getImagesByType(ImageType.SYSTEM_DOGAM_PROFILE, null, 0L);

        // cardId -> FileUrl 매핑
        Map<Long, String> dogamProfileMap = allDogamProfiles.stream()
                .collect(
                        Collectors.toMap(img ->
                                Long.valueOf(img.getS3Key().split("/")[2]),
                                ImageResponse::getFileUrl,
                                (v1, v2) -> v1 ) // 혹시라도 동일 cardId에 도감 프로필이 여러 개 있을 경우, 첫 번째 값만 사용
                );

        return cards.stream()
                .map(card ->
                        EncyclopediaCardSummaryResponse.builder()
                                .id(card.getId())
                                .name(card.getName())
                                .type(card.getType().getDescription())
                                .dogamProfileUrl(dogamProfileMap.get(card.getId()))
                                .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

        List<String> imageUrls = imageService.getImagesByType(
                        ImageType.SYSTEM_DOGAM,
                        null,
                        card.getId()
                ).stream()
                .map(ImageResponse::getFileUrl)
                .toList();

        return EncyclopediaCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .type(card.getType().getDescription())
                .size(card.getSize())
                .appearPeriod(card.getAppearPeriod())
                .place(card.getPlace())
                .imageUrls(imageUrls)
                .appearance(Optional.ofNullable(card.getAppearance()).map(AppearanceResponse::from).orElse(null))
                .personality(Optional.ofNullable(card.getPersonality()).map(PersonalityResponse::from).orElse(null))
                .significant(Optional.ofNullable(card.getSignificant()).map(SignificantResponse::from).orElse(null))
                .build();
    }


}
