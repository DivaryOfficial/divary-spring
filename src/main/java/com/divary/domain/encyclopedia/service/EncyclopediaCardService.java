package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardSummaryResponse;
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
    public EncyclopediaCardSummaryResponse getSummary(Long id) {
        return encyclopediaCardRepository.findById(id)
                .map(EncyclopediaCardSummaryResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUMMARY_NOT_FOUND));
    }

}
