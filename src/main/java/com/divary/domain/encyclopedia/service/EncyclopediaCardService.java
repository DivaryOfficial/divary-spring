package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
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
    public EncyclopediaCardResponse findById(Long id) {
        return encyclopediaCardRepository.findById(id)
                .map(EncyclopediaCardResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.CARD_NOT_FOUND));
    }
}
