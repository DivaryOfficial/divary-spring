package com.divary.domain.system.controller;

import com.divary.common.response.ApiResponse;
import com.divary.global.config.SwaggerConfig.ApiErrorExamples;
import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "System", description = "시스템 관리 및 모니터링")
@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemController {
    private final DataSource dataSource;

    @Operation(summary = "헬스 체크", description = "서비스 및 DB 상태를 확인합니다.")
    @ApiErrorExamples({
            ErrorCode.INTERNAL_SERVER_ERROR,
    })
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        String dbStatus = "UNKNOWN";
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                dbStatus = "UP";
            } else {
                dbStatus = "DOWN";
            }
        } catch (SQLException e) {
            dbStatus = "DOWN";
        }
        healthInfo.put("status", "UP");
        healthInfo.put("db", dbStatus);
        healthInfo.put("timestamp", System.currentTimeMillis());
        healthInfo.put("service", "divary-spring");

        return ApiResponse.success("서비스가 정상적으로 동작 중입니다.", healthInfo);
    }

    @Operation(summary = "에러 테스트", description = "에러 처리를 테스트합니다.")
    @ApiErrorExamples({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.INTERNAL_SERVER_ERROR
    })
    @GetMapping("/test-error")
    public ApiResponse<Void> testError() {
        throw new RuntimeException("테스트 에러입니다.");
    }

    @Operation(summary = "유효성 검증 테스트", description = "입력값 유효성 검증을 테스트합니다.")
    @ApiErrorExamples({
            ErrorCode.VALIDATION_ERROR,
            ErrorCode.REQUIRED_FIELD_MISSING,
            ErrorCode.INVALID_INPUT_VALUE
    })
    @GetMapping("/validation-test")
    public ApiResponse<String> validationTest(
            @Parameter(description = "테스트할 값", example = "test")
            @RequestParam String value
    ) {
        if (value.isEmpty()) {
            throw new RuntimeException("빈 값은 허용되지 않습니다.");
        }
        return ApiResponse.success("유효성 검증 통과", value);
    }

}