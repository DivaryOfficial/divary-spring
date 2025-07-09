package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.entity.EncyclopediaCard;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EncyclopediaCardService {

    private final EncyclopediaCardRepository encyclopediaCardRepository;

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
