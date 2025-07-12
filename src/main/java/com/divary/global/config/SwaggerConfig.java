package com.divary.global.config;

import com.divary.global.exception.ErrorCode;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import lombok.Builder;
import lombok.Getter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Divary API")
                        .description("다이빙 서포트 앱 Divary의 REST API 문서")
                        .version("v1.0.0"));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (Operation operation, HandlerMethod handlerMethod) -> {
            // 단일 에러 코드 어노테이션 처리
            ApiErrorExample apiErrorExample = handlerMethod.getMethodAnnotation(ApiErrorExample.class);
            if (apiErrorExample != null) {
                generateErrorCodeResponseExample(operation, new ErrorCode[]{apiErrorExample.value()});
            }

            // 복수 에러 코드 어노테이션 처리
            ApiErrorExamples apiErrorExamples = handlerMethod.getMethodAnnotation(ApiErrorExamples.class);
            if (apiErrorExamples != null) {
                generateErrorCodeResponseExample(operation, apiErrorExamples.value());
            }

            return operation;
        };
    }

    /**
     * 에러 코드들을 기반으로 Swagger 응답 예제를 생성
     */
    private void generateErrorCodeResponseExample(Operation operation, ErrorCode[] errorCodes) {
        ApiResponses responses = operation.getResponses();

        // HTTP 상태 코드별로 에러 코드들을 그룹화
        Map<Integer, List<ExampleHolder>> statusWithExampleHolders = Arrays.stream(errorCodes)
                .map(errorCode -> ExampleHolder.builder()
                        .example(createErrorExample(errorCode))
                        .name(errorCode.name())
                        .httpStatus(errorCode.getStatus().value())
                        .build())
                .collect(Collectors.groupingBy(ExampleHolder::getHttpStatus));

        // 상태 코드별로 ApiResponse에 예제들 추가
        addExamplesToResponses(responses, statusWithExampleHolders);
    }

    // ErrorCode를 기반으로 Example 객체 생성
    private Example createErrorExample(ErrorCode errorCode) {
        // 에러 응답 객체 생성
        Map<String, Object> errorResponse = new LinkedHashMap<>();
        errorResponse.put("timestamp", "2025-06-30T12:00:00.000000");
        errorResponse.put("status", errorCode.getStatus().value());
        errorResponse.put("code", errorCode.getCode());
        errorResponse.put("message", errorCode.getMessage());
        errorResponse.put("path", "/api/example");

        Example example = new Example();
        example.description(errorCode.getMessage());
        example.setValue(errorResponse);
        
        return example;
    }

    // 상태 코드별로 그룹화된 예제들을 ApiResponses에 추가
    private void addExamplesToResponses(ApiResponses responses, Map<Integer, List<ExampleHolder>> statusWithExampleHolders) {
        statusWithExampleHolders.forEach((httpStatus, exampleHolders) -> {
            // 해당 상태 코드에 대한 ApiResponse가 이미 존재하는지 확인
            String statusKey = httpStatus.toString();
            ApiResponse apiResponse = responses.get(statusKey);
            
            if (apiResponse == null) {
                apiResponse = new ApiResponse();
                apiResponse.setDescription("에러 응답");
                apiResponse.setContent(new Content());
            }

            // Content와 MediaType 설정
            Content content = apiResponse.getContent();
            MediaType mediaType = content.get("application/json");
            
            if (mediaType == null) {
                mediaType = new MediaType();
                content.addMediaType("application/json", mediaType);
            }

            // Examples 맵 설정
            Map<String, Example> examples = mediaType.getExamples();
            if (examples == null) {
                examples = new HashMap<>();
                mediaType.setExamples(examples);
            }

            // 각 에러 코드별 예제 추가
            for (ExampleHolder exampleHolder : exampleHolders) {
                examples.put(exampleHolder.getName(), exampleHolder.getExample());
            }

            // ApiResponse를 responses에 추가
            responses.addApiResponse(statusKey, apiResponse);
        });
    }

    // 예제 정보를 담는 내부 클래스
    @Getter
    @Builder
    private static class ExampleHolder {
        private final Example example;
        private final String name;
        private final int httpStatus;
    }

    
    // 단일 에러 코드 예제를 위한 어노테이션
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ApiErrorExample {
        ErrorCode value();
    }

    // 복수 에러 코드 예제를 위한 어노테이션
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ApiErrorExamples {
        ErrorCode[] value();
    }
}
