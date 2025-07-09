package com.divary.domain.encyclopedia.service;

import com.divary.domain.encyclopedia.dto.EncyclopediaCardResponse;
import com.divary.domain.encyclopedia.repository.EncyclopediaCardRepository;
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
                .orElseThrow(() -> new IllegalArgumentException("도감카드를 찾을 수 없습니다. id=" + id));
    }
}
