package com.example.toremainserver.service;

import com.example.toremainserver.dto.game.NpcChatRequest;
import com.example.toremainserver.dto.game.NpcChatResponse;
import com.example.toremainserver.dto.game.Ue5NpcRequest;
import com.example.toremainserver.dto.game.Ue5NpcResponse;
import com.example.toremainserver.entity.Conversation;
import com.example.toremainserver.entity.Npc;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.ConversationRepository;
import com.example.toremainserver.repository.NpcRepository;
import com.example.toremainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameEventService {
    private final RestTemplate restTemplate;
    private final String aiServerUrl;
    private final NpcRepository npcRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;

    @Autowired
    public GameEventService(RestTemplate restTemplate, @Value("${ai.server.url}") String aiServerUrl, 
                           NpcRepository npcRepository, UserRepository userRepository, 
                           ConversationRepository conversationRepository) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
        this.npcRepository = npcRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * UE5에서 받은 NPC 대화 정보를 파이썬 AI 서버로 그대로 전달하고,
     * 파이썬 AI 서버의 응답을 UE5로 반환합니다.
     * (게이트웨이 역할)
     * @param ue5Request UE5에서 받은 NPC 대화 요청 정보
     * @return UE5용 NPC 응답(Ue5NpcResponse)
     */
    public ResponseEntity<Ue5NpcResponse> forwardNpcRequest(Ue5NpcRequest ue5Request) {
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
        Optional<User> userOptional = userRepository.findById(ue5Request.getUserId());
        String playerName = "Unknown";
        String playerDescription = "모험가"; // 기본값
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            playerName = user.getPlayername();
            playerDescription = user.getPlayername() + " (레벨: " + user.getId() + ")";
        }
        
        String systemMessages = "시스템 메시지"; // DB에서 조회
        
        // currentPlayerMessage에서 message 추출
        String currentMessage = "";
        if (ue5Request.getCurrentPlayerMessage() != null) {
            currentMessage = ue5Request.getCurrentPlayerMessage().getMessage();
        }
        
        // DB에서 Conversation 조회하여 이전 대화 기록과 요약 가져오기
        Conversation conversation = conversationRepository.findByUserIdAndNpcId(
            ue5Request.getUserId(), 
            ue5Request.getNpcId()
        ).orElse(null);
        
        // 첫 대화인 경우 (Conversation이 없으면) userId, npcId로 새로 생성
        if (conversation == null) {
            conversation = new Conversation(ue5Request.getUserId(), ue5Request.getNpcId());
            conversationRepository.save(conversation);
        }
        
        // 이전 대화 기록과 요약 가져오기
        List<NpcChatRequest.ChatHistory> previousChatHistory = null;
        String previousConversationSummary = null;
        
        if (conversation.getRecentHistory() != null && !conversation.getRecentHistory().isEmpty()) {
            // recentHistory를 NpcChatRequest.ChatHistory로 변환
            previousChatHistory = convertConversationChatHistory(conversation.getRecentHistory());
        }
        
        if (conversation.getSummary() != null && !conversation.getSummary().isEmpty()) {
            previousConversationSummary = conversation.getSummary();
        }
        
        // 완전한 NpcChatRequest 구성 (DB에서 가져온 대화 기록 사용)
        NpcChatRequest npcChatRequest = new NpcChatRequest(
            ue5Request.getNpcId(),
            npcName,
            playerName,
            npcDescription,
            playerDescription,
            systemMessages,
            currentMessage,
            previousChatHistory,
            previousConversationSummary,
            ue5Request.getApiKey()
        );
        
        // 요청 헤더 설정 (JSON)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<NpcChatRequest> request = new HttpEntity<>(npcChatRequest, headers);
        // 파이썬 AI 서버의 NPC 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/npc";
        ResponseEntity<NpcChatResponse> response = restTemplate.postForEntity(url, request, NpcChatResponse.class);
        
        // AI 서버 응답이 성공적이고 responseBody가 있으면 Conversation 업데이트
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            updateConversationHistory(ue5Request, response.getBody());
        }
        
        // NpcChatResponse를 Ue5NpcResponse로 변환
        Ue5NpcResponse ue5Response = convertToUe5NpcResponse(response.getBody());
        
        // 처리된 응답을 UE5로 반환
        return ResponseEntity.status(response.getStatusCode()).body(ue5Response);
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
     * Conversation.ChatHistory 리스트를 NpcChatRequest.ChatHistory 리스트로 변환
     */
    private List<NpcChatRequest.ChatHistory> convertConversationChatHistory(List<Conversation.ChatHistory> conversationChatHistory) {
        if (conversationChatHistory == null) return null;
        
        return conversationChatHistory.stream()
            .map(chat -> new NpcChatRequest.ChatHistory(
                chat.getSpeaker(),
                chat.getMessage(),
                chat.getTimestamp()
            ))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * NpcChatResponse를 Ue5NpcResponse로 변환합니다.
     * @param npcChatResponse AI 서버 응답
     * @return UE5용 응답
     */
    private Ue5NpcResponse convertToUe5NpcResponse(NpcChatResponse npcChatResponse) {
        if (npcChatResponse == null) {
            return null;
        }
        
        // npcResponse 변환
        Ue5NpcResponse.ChatHistory ue5NpcResponse = null;
        if (npcChatResponse.getNpcResponse() != null) {
            ue5NpcResponse = new Ue5NpcResponse.ChatHistory(
                npcChatResponse.getNpcResponse().getSpeaker(),
                npcChatResponse.getNpcResponse().getMessage(),
                npcChatResponse.getNpcResponse().getTimestamp()
            );
        }
        
        // Ue5NpcResponse 생성
        return new Ue5NpcResponse(
            npcChatResponse.getNpcId(),
            ue5NpcResponse
        );
    }
    
    /**
     * userId와 npcId로 Conversation을 조회합니다.
     * @param userId 유저 ID
     * @param npcId NPC ID
     * @return Conversation (없으면 null)
     */
    public Conversation getConversation(Long userId, Long npcId) {
        return conversationRepository.findByUserIdAndNpcId(userId, npcId).orElse(null);
    }
    
    /**
     * Conversation의 recentHistory를 업데이트합니다.
     * currentPlayerMessage와 npcResponse를 추가하고,
     * 22개 이상이 되면 가장 오래된 2개를 제거합니다.
     * @param ue5Request UE5 요청
     * @param npcChatResponse AI 서버 응답
     */
    private void updateConversationHistory(Ue5NpcRequest ue5Request, NpcChatResponse npcChatResponse) {
        Long userId = ue5Request.getUserId();
        Long npcId = ue5Request.getNpcId();
        
        // Conversation 조회 또는 생성
        Conversation conversation = conversationRepository.findByUserIdAndNpcId(userId, npcId)
            .orElse(new Conversation(userId, npcId));
        
        // 기존 recentHistory 가져오기 (null이면 새 리스트 생성)
        List<Conversation.ChatHistory> recentHistory = conversation.getRecentHistory();
        if (recentHistory == null) {
            recentHistory = new ArrayList<>();
        } else {
            // 수정 가능한 리스트로 복사
            recentHistory = new ArrayList<>(recentHistory);
        }
        
        // 1. currentPlayerMessage 추가
        if (ue5Request.getCurrentPlayerMessage() != null) {
            Conversation.ChatHistory playerMessage = new Conversation.ChatHistory(
                ue5Request.getCurrentPlayerMessage().getSpeaker(),
                ue5Request.getCurrentPlayerMessage().getMessage(),
                ue5Request.getCurrentPlayerMessage().getTimestamp()
            );
            recentHistory.add(playerMessage);
        }
        
        // 2. npcResponse 추가
        if (npcChatResponse.getNpcResponse() != null) {
            Conversation.ChatHistory npcResponse = new Conversation.ChatHistory(
                npcChatResponse.getNpcResponse().getSpeaker(),
                npcChatResponse.getNpcResponse().getMessage(),
                npcChatResponse.getNpcResponse().getTimestamp()
            );
            recentHistory.add(npcResponse);
        }
        
        // 3. 22개 이상이면 앞의 2개 제거
        if (recentHistory.size() >= 22) {
            recentHistory = new ArrayList<>(recentHistory.subList(2, recentHistory.size()));
            
            // 4. summary 업데이트
            if (npcChatResponse.getPreviousConversationSummary() != null) {
                conversation.setSummary(npcChatResponse.getPreviousConversationSummary());
            }
        }
        
        // Conversation 업데이트
        conversation.setRecentHistory(recentHistory);
        
        // DB에 저장
        conversationRepository.save(conversation);
    }
    
} 