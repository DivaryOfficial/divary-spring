package com.divary.domain.logbase;

import com.divary.global.exception.BusinessException;
import com.divary.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogBaseInfoService {

    private final LogBaseInfoRepository logBaseInfoRepository;

    public LogBaseInfo validateAccess(Long logBaseInfoId, Long userId) {
        LogBaseInfo logBaseInfo = logBaseInfoRepository.findById(logBaseInfoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOG_BASE_NOT_FOUND));

        if (!logBaseInfo.getMember().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.LOG_BASE_FORBIDDEN_ACCESS);
        }

        return logBaseInfo;
    }
}
