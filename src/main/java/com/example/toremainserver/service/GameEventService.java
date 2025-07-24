package com.example.toremainserver.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class GameEventService {
    private final RestTemplate restTemplate;
    private final String aiServerUrl;

    @Autowired
    public GameEventService(RestTemplate restTemplate, @Value("${ai.server.url}") String aiServerUrl) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
    }

    /**
     * UE5에서 받은 NPC 대화 정보를 파이썬 AI 서버로 그대로 전달하고,
     * 파이썬 AI 서버의 응답을 UE5로 반환합니다.
     * (게이트웨이 역할)
     * @param body UE5에서 받은 npcId, history 등 정보
     * @return 파이썬 AI 서버의 응답(JSON)
     */
    public ResponseEntity<?> forwardNpcRequest(Map<String, Object> body) {
        // 요청 헤더 설정 (JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        // 파이썬 AI 서버의 NPC 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/npc";
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        // 파이썬 AI 서버의 응답을 그대로 반환
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }

    /**
     * UE5에서 받은 질감/색 정보를 파이썬 AI 서버로 그대로 전달하고,
     * 파이썬 AI 서버의 응답(multipart/form-data)을 UE5로 반환합니다.
     * (게이트웨이 역할)
     * @param body UE5에서 받은 description 등 정보
     * @return 파이썬 AI 서버의 응답(multipart/form-data)
     */
    public ResponseEntity<?> forwardMaterialRequest(Map<String, Object> body) {
        // 요청 헤더 설정 (JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        // 파이썬 AI 서버의 Material 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/material";
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, request, byte[].class);
        // 응답 헤더 설정 (multipart/form-data)
        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 파이썬 AI 서버의 응답을 그대로 반환
        return new ResponseEntity<>(response.getBody(), respHeaders, response.getStatusCode());
    }
} 