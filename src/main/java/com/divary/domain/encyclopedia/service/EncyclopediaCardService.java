package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
import com.divary.domain.encyclopedia.dto.PersonalityResponse;
import com.divary.domain.encyclopedia.dto.SignificantResponse;
import com.divary.domain.encyclopedia.entity.Appearance;
import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.entity.Personality;
import com.divary.domain.encyclopedia.entity.Significant;
import com.divary.domain.encyclopedia.enums.Type;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
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

    public static boolean isValidType(String name) {
        return Arrays.stream(Type.values()).anyMatch(t -> t.name().equals(name));
    }

    @Transactional(readOnly = true)
    public List<EncyclopediaCardSummaryResponse > getCards(String type) {
        if (type == null) {
            return encyclopediaCardRepository.findAll().stream()
                    .map(EncyclopediaCardSummaryResponse::from)
                    .toList();
        }

        if (!isValidType(type)) {
            throw new BusinessException(ErrorCode.TYPE_NOT_FOUND);
        }

        return encyclopediaCardRepository.findAllByType(type).stream()
                .map(EncyclopediaCardSummaryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
        return EncyclopediaCardResponse.from(card);
    }

}
