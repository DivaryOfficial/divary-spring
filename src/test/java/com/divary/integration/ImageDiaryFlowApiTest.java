package com.divary.integration;

import com.divary.common.response.ApiResponse;
import com.divary.domain.logbase.logbook.dto.request.LogBaseCreateRequestDTO;
import com.divary.domain.logbase.logbook.enums.IconType;
import com.divary.domain.logbase.logdiary.dto.DiaryRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.boot.test.web.client.TestRestTemplate;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ImageDiaryFlowApiTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    @Autowired
    ObjectMapper objectMapper;

    String baseUrl;
    String authToken;
    String email;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/v1";
        email = "test+" + System.currentTimeMillis() + "@divary.com";
        createTestUser(email);
        authToken = generateToken(email);
        assertNotNull(authToken);
    }

    @Test
    @DisplayName("단일/다중 이미지 temp→permanent 변환 및 재요청/교체 시 정상 동작")
    void imageDiaryEndToEndFlow() throws Exception {
        // 1) 로그 생성 (매번 다른 날짜)
        long logBaseId = createLogBase(LocalDate.now().plusDays(1));

        // 2) 임시 이미지 1장 업로드 → 다이어리 생성
        List<String> tempUrls1 = uploadTemps(1);
        String createdContent = createDiaryWithTemps(logBaseId, tempUrls1);
        assertTrue(createdContent.contains("/diary/" + logBaseId + "/"));
        assertFalse(createdContent.contains("/temp/"));

        // 3) 같은 본문으로 PUT(변경 없음) → 삭제되지 않고 유지되는지 확인
        String unchanged = updateDiaryWithContent(logBaseId, createdContent);
        assertEquals(createdContent, unchanged, "동일 본문이면 내용 불변이어야 함");

        // 4) 새 temp 업로드 1장 → 기존 영구를 새 temp로 교체
        List<String> tempUrls2 = uploadTemps(1);
        String replaced = updateDiaryReplacingWithTemps(logBaseId, unchanged, tempUrls2);
        assertTrue(replaced.contains("/diary/" + logBaseId + "/"));
        assertFalse(replaced.contains("/temp/"));
        // 기존 영구 키가 사라졌는지(간접 확인): 이전 콘텐츠에 있던 파일명이 더 이상 포함되지 않는지 확인
        assertFalse(replaced.equals(unchanged), "교체되었다면 콘텐츠가 변경되어야 함");

        // 5) 다중 이미지(2장, 3장)
        long logBaseId2 = createLogBase(LocalDate.now().plusDays(2));
        List<String> temp2 = uploadTemps(2);
        String c2 = createDiaryWithTemps(logBaseId2, temp2);
        assertTrue(countOccurrences(c2, "/diary/" + logBaseId2 + "/") >= 2);
        assertFalse(c2.contains("/temp/"));

        long logBaseId3 = createLogBase(LocalDate.now().plusDays(3));
        List<String> temp3 = uploadTemps(3);
        String c3 = createDiaryWithTemps(logBaseId3, temp3);
        assertTrue(countOccurrences(c3, "/diary/" + logBaseId3 + "/") >= 3);
        assertFalse(c3.contains("/temp/"));
    }

    // === Helpers ===

    void createTestUser(String email) {
        String url = baseUrl + "/system/test-user?email=" + encode(email);
        ResponseEntity<ApiResponse> resp = rest.postForEntity(url, null, ApiResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
    }

    String generateToken(String email) {
        String url = baseUrl + "/system/test-token?email=" + encode(email);
        ResponseEntity<ApiResponse> resp = rest.postForEntity(url, null, ApiResponse.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        return (String) Objects.requireNonNull(resp.getBody()).getData();
    }

    long createLogBase(LocalDate date) throws Exception {
        String url = baseUrl + "/logs";
        LogBaseCreateRequestDTO body = LogBaseCreateRequestDTO.builder()
                .iconType(IconType.CLOWNFISH)
                .name("해양일지-" + System.currentTimeMillis())
                .date(date)
                .build();
        HttpEntity<LogBaseCreateRequestDTO> req = new HttpEntity<>(body, authHeaders());
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("data").path("logBaseInfoId").asLong();
    }

    List<String> uploadTemps(int count) throws Exception {
        String url = baseUrl + "/images/upload/temp";
        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        for (int i = 0; i < count; i++) {
            ByteArrayResource res = new ByteArrayResource(makePngBytes(2 + i, 2 + i)) {
                @Override
                public String getFilename() {
                    return "t" + System.nanoTime() + ".png";
                }
            };
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.IMAGE_PNG);
            HttpEntity<ByteArrayResource> part = new HttpEntity<>(res, partHeaders);
            form.add("files", part);
        }
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> req = new HttpEntity<>(form, headers);
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode root = objectMapper.readTree(resp.getBody());
        List<String> urls = new ArrayList<>();
        for (JsonNode n : root.path("data").path("images")) {
            urls.add(n.path("fileUrl").asText());
        }
        return urls;
    }

    String createDiaryWithTemps(long logBaseId, List<String> tempUrls) throws Exception {
        String url = baseUrl + "/logs/" + logBaseId + "/diary";
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("type", "text", "rtfData", "e1xydGYx..."));
        for (String temp : tempUrls) {
            contents.add(Map.of(
                    "type", "image",
                    "data", Map.of(
                            "tempFilename", temp,
                            "caption", "바다 거북이와 함께",
                            "frameColor", "0",
                            "date", LocalDate.now().toString()
                    )
            ));
        }
        DiaryRequest body = new DiaryRequest();
        body.setContents((List) contents);
        HttpEntity<DiaryRequest> req = new HttpEntity<>(body, authHeaders());
        ResponseEntity<String> resp = rest.postForEntity(url, req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("data").path("contents").toString();
    }

    String updateDiaryWithContent(long logBaseId, String contentJson) throws Exception {
        String url = baseUrl + "/logs/" + logBaseId + "/diary";
        List<Map<String, Object>> contents = objectMapper.readValue(contentJson, new TypeReference<List<Map<String, Object>>>(){});
        DiaryRequest body = new DiaryRequest();
        body.setContents((List) contents);
        HttpEntity<DiaryRequest> req = new HttpEntity<>(body, authHeaders());
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.PUT, req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("data").path("contents").toString();
    }

    String updateDiaryReplacingWithTemps(long logBaseId, String currentContentJson, List<String> newTemps) throws Exception {
        List<Map<String, Object>> contents = objectMapper.readValue(currentContentJson, new TypeReference<List<Map<String, Object>>>(){});
        // 모든 이미지 항목의 tempFilename을 새 temp로 교체(새 temp가 1개면 라운드로빈)
        List<String> temps = new ArrayList<>(newTemps);
        int idx = 0;
        for (Map<String, Object> node : contents) {
            Object type = node.get("type");
            if ("image".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) node.get("data");
                data.put("tempFilename", temps.get(idx % temps.size()));
                idx++;
            }
        }
        DiaryRequest body = new DiaryRequest();
        body.setContents((List) contents);
        String url = baseUrl + "/logs/" + logBaseId + "/diary";
        HttpEntity<DiaryRequest> req = new HttpEntity<>(body, authHeaders());
        ResponseEntity<String> resp = rest.exchange(url, HttpMethod.PUT, req, String.class);
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode root = objectMapper.readTree(resp.getBody());
        return root.path("data").path("contents").toString();
    }

    HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    static String encode(String s) {
        return s.replace("@", "%40");
    }

    static byte[] makePngBytes(int w, int h) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    static int countOccurrences(String str, String sub) {
        int count = 0, from = 0;
        while (true) {
            int i = str.indexOf(sub, from);
            if (i < 0) return count;
            count++; from = i + sub.length();
        }
    }
}


