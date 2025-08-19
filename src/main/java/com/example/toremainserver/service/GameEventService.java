package com.example.toremainserver.service;

import com.example.toremainserver.dto.NpcChatRequest;
import com.example.toremainserver.dto.NpcChatResponse;
import com.example.toremainserver.dto.Ue5NpcRequest;
import com.example.toremainserver.entity.Npc;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.NpcRepository;
import com.example.toremainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameEventService {
    private final RestTemplate restTemplate;
    private final String aiServerUrl;
    private final NpcRepository npcRepository;
    private final UserRepository userRepository;

    @Autowired
    public GameEventService(RestTemplate restTemplate, @Value("${ai.server.url}") String aiServerUrl, NpcRepository npcRepository, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
        this.npcRepository = npcRepository;
        this.userRepository = userRepository;
    }

    /**
     * UE5에서 받은 NPC 대화 정보를 파이썬 AI 서버로 그대로 전달하고,
     * 파이썬 AI 서버의 응답을 UE5로 반환합니다.
     * (게이트웨이 역할)
     * @param ue5Request UE5에서 받은 NPC 대화 요청 정보
     * @return 파이썬 AI 서버의 응답(NpcChatResponse)
     */
    public ResponseEntity<NpcChatResponse> forwardNpcRequest(Ue5NpcRequest ue5Request) {
        // DB에서 NPC 정보 조회
        Optional<Npc> npcOptional = npcRepository.findByNpcId(ue5Request.getNpcId());
        
        if (npcOptional.isEmpty()) {
            // NPC가 존재하지 않는 경우 에러 응답
            return ResponseEntity.badRequest().body(null);
        }
        
        Npc npc = npcOptional.get();
        String npcName = npc.getName();
        
        // npcInfo에서 description 추출
        String npcDescription = "";
        if (npc.getNpcInfo() != null && npc.getNpcInfo().containsKey("description")) {
            npcDescription = (String) npc.getNpcInfo().get("description");
        }
        
        // DB에서 User 정보 조회
        Optional<User> userOptional = userRepository.findByPlayername(ue5Request.getPlayerName());
        String playerDescription = "모험가"; // 기본값
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            playerDescription = user.getPlayername() + " (레벨: " + user.getId() + ")";
        }
        
        String systemMessages = "시스템 메시지"; // DB에서 조회
        
        // 완전한 NpcChatRequest 구성 (previousConversationSummary 포함, apiKey 포함)
        NpcChatRequest npcChatRequest = new NpcChatRequest(
            convertChatType(ue5Request.getChatType()),
            ue5Request.getNpcId(),
            npcName,
            ue5Request.getPlayerName(),
            npcDescription,
            playerDescription,
            systemMessages,
            ue5Request.getCurrentPlayerMessage(),
            convertChatHistory(ue5Request.getPreviousChatHistory()),
            ue5Request.getPreviousConversationSummary(),
            ue5Request.getApiKey()
        );
        
        // 요청 헤더 설정 (JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NpcChatRequest> request = new HttpEntity<>(npcChatRequest, headers);
        // 파이썬 AI 서버의 NPC 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/npc";
        ResponseEntity<NpcChatResponse> response = restTemplate.postForEntity(url, request, NpcChatResponse.class);
        
        // AI 서버 응답 처리
        NpcChatResponse responseBody = response.getBody();
        if (responseBody != null) {
            processChatHistory(responseBody);
        }
        
        // 처리된 응답을 UE5로 반환
        return ResponseEntity.status(response.getStatusCode()).body(responseBody);
    }

    /**
     * UE5에서 받은 질감/색 정보를 파이썬 AI 서버로 그대로 전달하고,
     * 파이썬 AI 서버의 응답(multipart/form-data)을 UE5로 반환합니다.
     * (게이트웨이 역할)
     * @param body UE5에서 받은 description 등 정보 (api_key 포함)
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
    
    /**
     * AI 서버 응답의 ChatHistory를 처리합니다.
     * ChatHistory가 40개를 넘으면 마지막 두 개의 ChatRecord를 삭제합니다.
     * @param response AI 서버 응답 객체
     */
    private void processChatHistory(NpcChatResponse response) {
        if (response.getChatHistory() != null) {
            List<NpcChatResponse.ChatRecord> chatHistory = response.getChatHistory();
            if (chatHistory.size() > 40) {
                // 마지막 두 개의 ChatRecord 삭제
                chatHistory = chatHistory.subList(0, chatHistory.size() - 2);
                response.setChatHistory(chatHistory);
            }
        }
    }
} 