package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
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
        return List.of("어류", "연체동물", "자포동물").contains(type);
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
                .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));
        return EncyclopediaCardResponse.summaryOf(card);
    }

    @Transactional(readOnly = true)
    public EncyclopediaCardResponse getDetail(Long id) {
        EncyclopediaCard card = encyclopediaCardRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.DETAIL_NOT_FOUND));
        return EncyclopediaCardResponse.detailOf(card);
    }
}
