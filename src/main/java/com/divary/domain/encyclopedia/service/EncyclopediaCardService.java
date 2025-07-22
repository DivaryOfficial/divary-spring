package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.enums.Type;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
import com.divary.domain.image.service.ImageService;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
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
        if (description == null) {
            return encyclopediaCardRepository.findAll().stream()
                    .map(card -> EncyclopediaCardSummaryResponse.from(card, imageService))
                    .toList();
        }

        if (!isValidDescription(description)) {
            throw new BusinessException(ErrorCode.TYPE_NOT_FOUND);
        }

        Type typeEnum = convertDescriptionToEnum(description);
        return encyclopediaCardRepository.findAllByType(typeEnum).stream()
                .map(card -> EncyclopediaCardSummaryResponse.from(card, imageService))
                .toList();
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
        return EncyclopediaCardResponse.from(card, imageService);
    }

}
