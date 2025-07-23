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
import com.divary.domain.image.entity.ImageType;
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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

        return cards.stream()
                .map(card -> {
                    String thumbnailUrl = imageService.getImagesByType(
                                    ImageType.SYSTEM_DOGAM_PROFILE,
                                    null,
                                    String.valueOf(card.getId())
                            ).stream()
                            .findFirst()
                            .map(ImageResponse::getFileUrl)
                            .orElse("");

                    return EncyclopediaCardSummaryResponse.builder()
                            .id(card.getId())
                            .name(card.getName())
                            .type(card.getType().getDescription())
                            .thumbnailUrl(thumbnailUrl)
                            .build();
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

        List<String> imageUrls = imageService.getImagesByType(
                        ImageType.SYSTEM_DOGAM,
                        null,
                        String.valueOf(card.getId())
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
