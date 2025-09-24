package com.divary.global.redis.controller;

import com.divary.global.redis.dto.RedisDto;
import com.divary.global.redis.service.RedisSingleDataService;
import lombok.RequiredArgsConstructor; // ⭐️ @AllArgsConstructor 대신 @RequiredArgsConstructor 권장
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/redis") // ⭐️ 공통 경로 단순화
@RequiredArgsConstructor
public class RedisSingleDataController {

    private final RedisSingleDataService redisSingleDataService;

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 조회합니다.
     *
     * @param key 조회할 데이터의 키
     * @return 조회된 데이터 값
     */
    // ⭐️ POST -> GET, @RequestBody -> @PathVariable
    @GetMapping("/{key}")
    public ResponseEntity<String> getValue(@PathVariable String key) {
        String value = redisSingleDataService.getSingleData(key);
        // ⭐️ 값이 없을 경우 404 Not Found, 있을 경우 200 OK 반환
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * Redis 단일 데이터 값을 등록/수정합니다.
     *
     * @param redisDto 키, 값, 유효 시간을 포함한 DTO
     * @return 성공 응답
     */
    // ⭐️ POST -> PUT (멱등성을 나타내기 위해)
    @PutMapping
    public ResponseEntity<Void> setValue(@RequestBody RedisDto redisDto) {
        // ⭐️ 서비스의 반환 타입이 void이므로 값을 받지 않음
        if (redisDto.getDuration() == null) {
            redisSingleDataService.setSingleData(redisDto.getKey(), redisDto.getValue());
        } else {
            redisSingleDataService.setSingleData(redisDto.getKey(), redisDto.getValue(), redisDto.getDuration());
        }
        // ⭐️ 성공 시 200 OK 와 함께 빈 Body를 반환
        return ResponseEntity.ok().build();
    }

    /**
     * Redis 키를 기반으로 단일 데이터의 값을 삭제합니다.
     *
     * @param key 삭제할 데이터의 키
     * @return 성공 응답
     */
    // ⭐️ @RequestBody -> @PathVariable
    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteValue(@PathVariable String key) { // ⭐️ deleteRow -> deleteValue (더 명확한 이름)
        // ⭐️ 서비스의 반환 타입이 boolean이므로 boolean으로 받음
        boolean isDeleted = redisSingleDataService.deleteSingleData(key);

        // ⭐️ 삭제 성공 시 204 No Content, 실패(키가 애초에 없음) 시 404 Not Found 반환
        return isDeleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}