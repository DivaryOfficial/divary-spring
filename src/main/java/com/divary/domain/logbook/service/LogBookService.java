package com.divary.domain.logbook.service;

import com.divary.domain.logbook.dto.LogBookRequestDTO;
import com.divary.domain.logbook.dto.LogBookResponseDTO;
import com.divary.domain.logbook.entity.LogBook;
import com.divary.domain.logbook.repository.LogBookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LogBookService {

    private final LogBookRepository logBookRepository;

    @Transactional
    public LogBookResponseDTO.CreateResultDTO createLog(LogBookRequestDTO.CreateDTO request) {

        LogBook logBook = LogBook.builder()
                .iconType(request.getIconType())
                .name(request.getName())
                .date(request.getDate()) // 현재 날짜로 설정
                .build();

        LogBook saved = logBookRepository.save(logBook);

        return LogBookResponseDTO.CreateResultDTO.builder()
                .name(saved.getName())
                .iconType(saved.getIconType())
                .date(saved.getDate())
                .build();
    }


}
