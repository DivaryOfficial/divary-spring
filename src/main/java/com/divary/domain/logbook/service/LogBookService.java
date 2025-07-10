package com.divary.domain.logbook.service;

import com.divary.domain.logbook.dto.LogBookRequestDTO;
import com.divary.domain.logbook.dto.LogBookResponseDTO;
import com.divary.domain.logbook.entity.LogBook;

public interface LogBookService {
    LogBookResponseDTO.CreatResultDTO createLog(LogBookRequestDTO.CreateDTO request);
}
