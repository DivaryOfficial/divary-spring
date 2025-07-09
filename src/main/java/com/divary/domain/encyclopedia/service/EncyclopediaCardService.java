package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.AppearanceResponse;
import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.entity.Appearance;
import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EncyclopediaCardService {

    private final EncyclopediaCardRepository encyclopediaCardRepository;

    private boolean isValidType(String type) {
        // TODO: 현재는 임시로 모든 타입을 허용하도록 true 반환
        // 추후 CardType enum 도입 시 유효성 검사 수정 예정
        return true;
    }

    @Transactional(readOnly = true)
    public List<EncyclopediaCardResponse> getCards(String type) {
        // 전체 조회
        if (type == null) {
            return encyclopediaCardRepository.findAll().stream()
                    .map(EncyclopediaCardResponse::summaryOf)
                    .toList();
        }

        // type 유효성 검사
        if (!isValidType(type)) {
            throw new BusinessException(ErrorCode.TYPE_NOT_FOUND);
        }

        return encyclopediaCardRepository.findAllByType(type).stream()
                .map(EncyclopediaCardResponse::summaryOf)
                .toList();
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getSummary(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
        return EncyclopediaCardResponse.summaryOf(card);
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
        return EncyclopediaCardResponse.detailOf(card);
    }

    @Transactional(readOnly = true)
    public AppearanceResponse getAppearance(Long cardId) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(cardId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));

        Appearance appearance = card.getAppearance();
        if (appearance == null) {
            throw new BusinessException(ErrorCode.CARD_APPEARANCE_NOT_FOUND);
        }

        return AppearanceResponse.builder()
                .body(appearance.getBody())
                .colorCodes(appearance.getColorCodes())
                .color(appearance.getColor())
                .pattern(appearance.getPattern())
                .etc(appearance.getEtc())
                .build();
    }

}
