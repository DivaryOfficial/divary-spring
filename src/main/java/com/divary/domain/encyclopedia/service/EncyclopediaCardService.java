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
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
    @Cacheable(cacheNames = com.divary.global.config.CacheConfig.CACHE_ENCYCLOPEDIA_SUMMARY, key = "#description != null ? #description : 'ALL'")
    public List<EncyclopediaCardSummaryResponse> getCards(String description) {
        List<EncyclopediaCard> cards;
        if (description == null) cards = encyclopediaCardRepository.findAll();

        else {
            if (!isValidDescription(description)) throw new BusinessException(ErrorCode.TYPE_NOT_FOUND);
            Type typeEnum = convertDescriptionToEnum(description);
            cards = encyclopediaCardRepository.findAllByType(typeEnum);
        }

        // 도감 프로필 전부 불러오기
        String pathPattern = "system/dogam_profile/";
        List<ImageResponse> allProfileImages = imageService.getImagesByPath(pathPattern);

        Map<Long, String> profileImageMap;
        if (description == null) {
            // 필터링이 없을 때
            profileImageMap = allProfileImages.stream()
                    .filter(image -> image.getPostId() != null)
                    .collect(Collectors.toMap(
                            ImageResponse::getPostId,
                            ImageResponse::getFileUrl,
                            (existingUrl, newUrl) -> existingUrl
                    ));
        } else {
            // 필터링이 있을 때: 조회된 카드 ID 목록을 먼저 추출
            Set<Long> cardIds = cards.stream()
                    .map(EncyclopediaCard::getId)
                    .collect(Collectors.toSet());

            // 해당 카드 ID를 가진 이미지들만 필터링하여
            profileImageMap = allProfileImages.stream()
                    .filter(image -> image.getPostId() != null && cardIds.contains(image.getPostId()))
                    .collect(Collectors.toMap(
                            ImageResponse::getPostId,
                            ImageResponse::getFileUrl,
                            (existingUrl, newUrl) -> existingUrl
                    ));
        }

        return cards.stream()
                .map(card ->
                        EncyclopediaCardSummaryResponse.builder()
                                .id(card.getId())
                                .name(card.getName())
                                .type(card.getType().getDescription())
                                .dogamProfileUrl(profileImageMap.get(card.getId()))
                                .build())
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = com.divary.global.config.CacheConfig.CACHE_ENCYCLOPEDIA_DETAIL, key = "#id")
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

        String pathPattern = "system/dogam/" + id + "/";

        List<String> imageUrls = imageService.getImagesByPath(pathPattern)
                .stream()
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
