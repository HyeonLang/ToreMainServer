package com.example.toremainserver.service;

import com.example.toremainserver.dto.NpcChatRequest;
import com.example.toremainserver.dto.NpcChatResponse;
import com.example.toremainserver.dto.Ue5NpcRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
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
     * @param ue5Request UE5에서 받은 NPC 대화 요청 정보
     * @return 파이썬 AI 서버의 응답(NpcChatResponse)
     */
    public ResponseEntity<NpcChatResponse> forwardNpcRequest(Ue5NpcRequest ue5Request) {
        // TODO: DB에서 NPC 정보 조회 (추후 구현)
        // NpcRepository에서 npcId로 NPC 정보 조회
        // UserRepository에서 playerName으로 플레이어 정보 조회
        
        // 임시로 하드코딩된 값들 (추후 DB 조회로 대체)
        String npcName = "상인"; // DB에서 조회
        String npcDescription = "마을의 상인"; // DB에서 조회
        String playerDescription = "모험가"; // DB에서 조회
        String systemMessages = "시스템 메시지"; // DB에서 조회
        
        // 완전한 NpcChatRequest 구성
        NpcChatRequest npcChatRequest = new NpcChatRequest(
            convertChatType(ue5Request.getChatType()),
            ue5Request.getNpcId(),
            npcName,
            ue5Request.getPlayerName(),
            npcDescription,
            playerDescription,
            systemMessages,
            ue5Request.getCurrentPlayerMessage(),
            convertChatHistory(ue5Request.getPreviousChatHistory())
        );
        
        // 요청 헤더 설정 (JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NpcChatRequest> request = new HttpEntity<>(npcChatRequest, headers);
        // 파이썬 AI 서버의 NPC 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/npc";
        ResponseEntity<NpcChatResponse> response = restTemplate.postForEntity(url, request, NpcChatResponse.class);
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
    
    /**
     * Ue5NpcRequest.ChatType을 NpcChatRequest.ChatType으로 변환
     */
    private NpcChatRequest.ChatType convertChatType(Ue5NpcRequest.ChatType ue5ChatType) {
        if (ue5ChatType == null) return null;
        
        switch (ue5ChatType) {
            case START:
                return NpcChatRequest.ChatType.START;
            case CONTINUE:
                return NpcChatRequest.ChatType.CONTINUE;
            case END:
                return NpcChatRequest.ChatType.END;
            default:
                throw new IllegalArgumentException("Unknown chat type: " + ue5ChatType);
        }
    }
    
    /**
     * Ue5NpcRequest.ChatHistory 리스트를 NpcChatRequest.ChatHistory 리스트로 변환
     */
    private List<NpcChatRequest.ChatHistory> convertChatHistory(List<Ue5NpcRequest.ChatHistory> ue5ChatHistory) {
        if (ue5ChatHistory == null) return null;
        
        return ue5ChatHistory.stream()
            .map(this::convertChatHistoryItem)
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Ue5NpcRequest.ChatHistory를 NpcChatRequest.ChatHistory로 변환
     */
    private NpcChatRequest.ChatHistory convertChatHistoryItem(Ue5NpcRequest.ChatHistory ue5ChatHistory) {
        if (ue5ChatHistory == null) return null;
        
        return new NpcChatRequest.ChatHistory(
            ue5ChatHistory.getSpeaker(),
            ue5ChatHistory.getMessage(),
            ue5ChatHistory.getTimestamp()
        );
    }
} 