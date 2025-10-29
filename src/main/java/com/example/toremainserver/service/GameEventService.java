package com.example.toremainserver.service;

import com.example.toremainserver.dto.game.NpcChatRequest;
import com.example.toremainserver.dto.game.NpcChatResponse;
import com.example.toremainserver.dto.game.Ue5NpcRequest;
import com.example.toremainserver.dto.game.Ue5NpcResponse;
import com.example.toremainserver.dto.game.ProfileCreateRequest;
import com.example.toremainserver.dto.game.UserGameProfileResponse;
import com.example.toremainserver.dto.game.UserGameProfileSyncRequest;
import com.example.toremainserver.dto.game.UserGameProfileUpdateRequest;
import com.example.toremainserver.dto.game.GoldUpdateRequest;
import com.example.toremainserver.dto.game.GoldUpdateResponse;
import com.example.toremainserver.dto.game.ExperienceUpdateRequest;
import com.example.toremainserver.dto.game.ExperienceUpdateResponse;
import com.example.toremainserver.dto.game.EquipmentSlotRequest;
import com.example.toremainserver.dto.game.EquipmentUpdateResponse;
import com.example.toremainserver.entity.Conversation;
import com.example.toremainserver.entity.Npc;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.entity.UserGameProfile;
import com.example.toremainserver.repository.ConversationRepository;
import com.example.toremainserver.repository.NpcRepository;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.repository.UserGameProfileRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GameEventService {
    private static final Logger logger = LoggerFactory.getLogger(GameEventService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate;
    private final String aiServerUrl;
    private final NpcRepository npcRepository;
    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final UserGameProfileRepository userGameProfileRepository;
    
    // ========================================
    // 권한 검증 헬퍼 메서드 (인증 시스템 연동 대비)
    // ========================================
    
    /**
     * 현재 로그인한 유저가 특정 userId로 작업을 수행할 권한이 있는지 검증합니다.
     * 인증 시스템이 추가되면 주석을 해제하여 사용합니다.
     * @param userId 검증할 userId
     */
    private void validateUserAuthorization(Long userId) {
        // TODO: 인증 시스템 추가 시 아래 주석 해제
        /*
        Long currentUserId = SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!currentUserId.equals(userId)) {
            throw new UnauthorizedException("Cannot perform action for another user");
        }
        */
    }
    
    /**
     * 현재 로그인한 유저가 특정 프로필의 소유자인지 검증합니다.
     * 인증 시스템이 추가되면 주석을 해제하여 사용합니다.
     * @param profile 검증할 프로필
     */
    private void validateProfileOwnership(UserGameProfile profile) {
        // TODO: 인증 시스템 추가 시 아래 주석 해제
        /*
        Long currentUserId = SecurityContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new UnauthorizedException("Authentication required");
        }
        if (!profile.getUserId().equals(currentUserId)) {
            throw new UnauthorizedException("This profile doesn't belong to you");
        }
        */
    }

    @Autowired
    public GameEventService(RestTemplate restTemplate, @Value("${ai.server.url}") String aiServerUrl, 
                           NpcRepository npcRepository, UserRepository userRepository, 
                           ConversationRepository conversationRepository,
                           UserGameProfileRepository userGameProfileRepository) {
        this.restTemplate = restTemplate;
        this.aiServerUrl = aiServerUrl;
        this.npcRepository = npcRepository;
        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.userGameProfileRepository = userGameProfileRepository;
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
        Optional<Npc> npcOptional = npcRepository.findById(ue5Request.getNpcId());
        
        if (npcOptional.isEmpty()) {
            // NPC가 존재하지 않는 경우 에러 응답
            return ResponseEntity.badRequest().body(null);
        }
        
        Npc npc = npcOptional.get();
        String npcName = npc.getName();
        
        // npcInfo에서 페르소나 정보를 추출하여 포맷팅
        String npcDescription = buildNpcDescription(npc);
        
        // DB에서 User 정보 조회
        Optional<User> userOptional = userRepository.findById(ue5Request.getUserId());
        String playerName = "Unknown";
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            playerName = user.getPlayername();
        }
        
        // UE5 요청의 playerDescription JSON을 포맷팅된 문자열로 변환
        String playerDescription = buildPlayerDescription(ue5Request.getPlayerDescription(), playerName);
        
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
        
        // LLM 서버 요청 로그 출력
        try {
            String requestJson = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(npcChatRequest);
            logger.info("=== LLM Server Request ===");
            logger.info("URL: {}/api.ai/llm/generate", aiServerUrl);
            logger.info("Request Body:\n{}", requestJson);
        } catch (Exception e) {
            logger.error("Failed to log LLM request", e);
        }
        
        // 파이썬 AI 서버의 NPC 엔드포인트로 POST 요청
        String url = aiServerUrl + "/api.ai/llm/generate";
        
        ResponseEntity<NpcChatResponse> response;
        try {
            response = restTemplate.postForEntity(url, request, NpcChatResponse.class);
            
            // LLM 서버 응답 로그 출력 (성공)
            try {
                String responseJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(response.getBody());
                logger.info("=== LLM Server Response (SUCCESS) ===");
                logger.info("Status Code: {}", response.getStatusCode());
                logger.info("Response Body:\n{}", responseJson);
            } catch (Exception e) {
                logger.error("Failed to log LLM response", e);
            }
            
            // AI 서버 응답이 성공적이고 responseBody가 있으면 Conversation 업데이트
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                updateConversationHistory(ue5Request, response.getBody());
            }
            
            // NpcChatResponse를 Ue5NpcResponse로 변환
            Ue5NpcResponse ue5Response = convertToUe5NpcResponse(response.getBody());
            
            // 처리된 응답을 UE5로 반환
            return ResponseEntity.status(response.getStatusCode()).body(ue5Response);
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // LLM 서버 에러 응답 로그 출력
            logger.error("=== LLM Server Response (ERROR) ===");
            logger.error("Status Code: {}", e.getStatusCode());
            logger.error("Error Response Body:\n{}", e.getResponseBodyAsString());
            throw e;
        }
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
     * User나 NPC가 존재하지 않으면 null 반환 (404),
     * 둘 다 존재하지만 Conversation이 없으면 빈 객체 반환 (200)
     * @param userId 유저 ID
     * @param npcId NPC ID
     * @return Conversation (User나 NPC가 없으면 null)
     */
    public Conversation getNpcConversations(Long userId, Long npcId) {
        // User 존재 여부 확인
        if (!userRepository.existsById(userId)) {
            return null; // User가 없으면 404
        }
        
        // NPC 존재 여부 확인
        if (!npcRepository.existsById(npcId)) {
            return null; // NPC가 없으면 404
        }
        
        // User와 NPC 모두 존재하면 Conversation 조회 또는 빈 객체 반환
        return conversationRepository.findByUserIdAndNpcId(userId, npcId)
            .orElse(new Conversation(userId, npcId));
    }
    
    /**
     * 모든 NPC 정보를 조회합니다.
     * @return 모든 NPC 리스트
     */
    public List<Npc> getAllNpcs() {
        return npcRepository.findAll();
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
        if (recentHistory.size() >= 8) {
            recentHistory = new ArrayList<>(recentHistory.subList(2, recentHistory.size()));
            
            // 4. summary 업데이트
            if (npcChatResponse.getPreviousConversationSummary() != null) {
                conversation.setSummary(npcChatResponse.getPreviousConversationSummary());
            }
        }
        
        // Conversation 업데이트
        conversation.setRecentHistory(recentHistory);
        conversation.setLastUpdated(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        
        // DB에 저장
        conversationRepository.save(conversation);
    }
    
    /**
     * NPC의 npcInfo를 AI 프롬프트 형식의 문자열로 변환합니다.
     * @param npc NPC 엔티티
     * @return 포맷팅된 NPC 설명 문자열
     */
    private String buildNpcDescription(Npc npc) {
        if (npc.getNpcInfo() == null || npc.getNpcInfo().isEmpty()) {
            return "";
        }
        
        Map<String, Object> npcInfo = npc.getNpcInfo();
        StringBuilder description = new StringBuilder();
        
        description.append("# 시스템 명령 및 페르소나 정의\n");
        description.append("당신은 이제부터 '").append(npc.getName()).append("'라는 ");
        description.append(getStringValue(npcInfo, "occupation", "NPC"));
        description.append(" NPC 역할을 수행해야 합니다.\n");
        description.append("다음의 정보를 철저히 준수하여 사용자와 대화하십시오.\n\n");
        
        description.append("## ").append(npc.getName()).append(" NPC 페르소나\n");
        description.append("* **이름:** ").append(npc.getName()).append("\n");
        description.append("* **성별:** ").append(getStringValue(npcInfo, "gender", "알 수 없음")).append("\n");
        description.append("* **나이:** ").append(getStringValue(npcInfo, "age", "알 수 없음")).append("세\n");
        description.append("* **위치:** ").append(getStringValue(npcInfo, "location", "알 수 없음")).append("\n");
        description.append("* **직업:** ").append(getStringValue(npcInfo, "occupation", "알 수 없음")).append("\n");
        description.append("* **성격:** ").append(getStringValue(npcInfo, "personality", "알 수 없음")).append("\n");
        
        String persona = getStringValue(npcInfo, "persona", "");
        if (!persona.isEmpty()) {
            description.append("* **스토리/캐릭터 배경:** ").append(persona).append("\n");
        }
        
        description.append("\n## 대화 스타일 (Speaking Style)\n");
        String speakingStyle = getStringValue(npcInfo, "speakingStyle", "평범한 말투로 대화합니다.");
        description.append("* **특징:** ").append(speakingStyle).append("\n");
        
        return description.toString();
    }
    
    /**
     * 플레이어의 상세 정보를 AI 프롬프트 형식의 문자열로 변환합니다.
     * @param playerDescriptionMap UE5에서 받은 플레이어 정보 맵
     * @param playerName 플레이어 이름
     * @return 포맷팅된 플레이어 설명 문자열
     */
    private String buildPlayerDescription(Map<String, Object> playerDescriptionMap, String playerName) {
        if (playerDescriptionMap == null || playerDescriptionMap.isEmpty()) {
            // 기본 설명 반환
            return "이름: " + playerName + "\n정보: 모험가";
        }
        
        StringBuilder description = new StringBuilder();
        
        description.append("## 플레이어 정보\n");
        description.append("* **이름:** ").append(playerName).append("\n");
        
        // 레벨 정보
        if (playerDescriptionMap.containsKey("level")) {
            description.append("* **레벨:** ").append(getStringValue(playerDescriptionMap, "level", "1")).append("\n");
        }
        
        // 직업/클래스 정보
        if (playerDescriptionMap.containsKey("class")) {
            description.append("* **직업:** ").append(getStringValue(playerDescriptionMap, "class", "모험가")).append("\n");
        }
        
        // 현재 위치
        if (playerDescriptionMap.containsKey("currentLocation")) {
            description.append("* **현재 위치:** ").append(getStringValue(playerDescriptionMap, "currentLocation", "알 수 없음")).append("\n");
        }
        
        // 소지금
        if (playerDescriptionMap.containsKey("gold")) {
            description.append("* **소지금:** ").append(getStringValue(playerDescriptionMap, "gold", "0")).append(" 골드\n");
        }
        
        // 평판
        if (playerDescriptionMap.containsKey("reputation")) {
            description.append("* **평판:** ").append(getStringValue(playerDescriptionMap, "reputation", "보통")).append("\n");
        }
        
        // 최근 업적
        if (playerDescriptionMap.containsKey("recentAchievement")) {
            description.append("* **최근 업적:** ").append(getStringValue(playerDescriptionMap, "recentAchievement", "없음")).append("\n");
        }
        
        // 현재 퀘스트
        if (playerDescriptionMap.containsKey("currentQuest")) {
            description.append("* **진행 중인 퀘스트:** ").append(getStringValue(playerDescriptionMap, "currentQuest", "없음")).append("\n");
        }
        
        // 추가 정보가 있다면 포함
        for (Map.Entry<String, Object> entry : playerDescriptionMap.entrySet()) {
            String key = entry.getKey();
            // 이미 처리한 필드는 건너뛰기
            if (!key.equals("playername") && !key.equals("level") && !key.equals("class") 
                && !key.equals("currentLocation") && !key.equals("gold") 
                && !key.equals("reputation") && !key.equals("recentAchievement") 
                && !key.equals("currentQuest")) {
                description.append("* **").append(key).append(":** ").append(entry.getValue()).append("\n");
            }
        }
        
        return description.toString();
    }
    
    /**
     * Map에서 문자열 값을 안전하게 추출합니다.
     * @param map 데이터 맵
     * @param key 키
     * @param defaultValue 기본값
     * @return 추출된 값 또는 기본값
     */
    private String getStringValue(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }
    
    /**
     * profileId로 UserGameProfile을 조회합니다.
     * @param profileId 프로필 ID
     * @return UserGameProfileResponse (프로필이 없으면 null)
     */
    public UserGameProfileResponse getUserGameProfile(Long profileId) {
        Optional<UserGameProfile> profileOptional = userGameProfileRepository.findById(profileId);
        
        if (profileOptional.isEmpty()) {
            return null;
        }
        
        UserGameProfile profile = profileOptional.get();
        
        // 소유권 검증
        validateProfileOwnership(profile);
        
        return new UserGameProfileResponse(
            profile.getId(),        // Entity는 id
            profile.getUserId(),
            profile.getProfileName(),
            profile.getLevel(),
            profile.getExperience(),
            profile.getGold(),
            profile.getEquippedItems(),
            profile.getSkillInfo(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
    
    /**
     * userId로 해당 유저의 모든 프로필 목록 조회 (간단 정보만)
     * profileId와 profileName만 반환하여 네트워크 효율 최적화
     */
    public List<Map<String, Object>> getUserGameProfiles(Long userId) {
        // 사용자 권한 검증
        validateUserAuthorization(userId);
        
        List<UserGameProfile> profiles = userGameProfileRepository.findByUserId(userId);
        
        return profiles.stream()
            .map(profile -> {
                Map<String, Object> summary = new java.util.HashMap<>();
                summary.put("profileId", profile.getId());
                summary.put("profileName", profile.getProfileName());
                return summary;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * 새 UserGameProfile을 생성합니다. (최초 1회 생성용)
     * @param request userId와 profileName
     * @return 생성된 UserGameProfileResponse
     */
    @Transactional
    public UserGameProfileResponse createUserGameProfile(ProfileCreateRequest request) {
        if (request.getUserId() == null || request.getProfileName() == null) {
            throw new IllegalArgumentException("userId and profileName are required");
        }
        
        // 권한 검증 (다른 유저의 프로필 생성 방지)
        validateUserAuthorization(request.getUserId());
        
        // 동일한 이름의 프로필이 이미 있는지 확인
        Optional<UserGameProfile> existing = userGameProfileRepository
            .findByUserIdAndProfileName(request.getUserId(), request.getProfileName());
        
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Profile with name '" + request.getProfileName() + "' already exists for this user");
        }
        
        // 신규 프로필 생성 (기본값으로 초기화)
        UserGameProfile profile = new UserGameProfile(request.getUserId(), request.getProfileName());
        // level, experience, gold 등은 Entity에서 기본값으로 초기화됨
        
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        return new UserGameProfileResponse(
            savedProfile.getId(),    // Entity는 id
            savedProfile.getUserId(),
            savedProfile.getProfileName(),
            savedProfile.getLevel(),
            savedProfile.getExperience(),
            savedProfile.getGold(),
            savedProfile.getEquippedItems(),
            savedProfile.getSkillInfo(),
            savedProfile.getCreatedAt(),
            savedProfile.getUpdatedAt()
        );
    }
    
    /**
     * 전체 UserGameProfile 정보를 동기화합니다.
     * profileId가 있으면 기존 프로필을 업데이트하고, 없으면 새로 생성합니다.
     * @param request 동기화할 전체 정보 (profileId, userId, profileName 등)
     * @return 저장된 UserGameProfileResponse
     */
    @Transactional
    public UserGameProfileResponse syncUserGameProfile(UserGameProfileSyncRequest request) {
        UserGameProfile profile;
        
        if (request.getProfileId() != null) {
            // 기존 프로필 업데이트
            profile = userGameProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new IllegalArgumentException("Profile not found: " + request.getProfileId()));
            
            // 소유권 검증
            validateProfileOwnership(profile);
        } else {
            // 신규 프로필 생성
            if (request.getUserId() == null || request.getProfileName() == null) {
                throw new IllegalArgumentException("userId and profileName are required for new profile");
            }
            
            // 권한 검증 (다른 유저의 프로필 생성 방지)
            validateUserAuthorization(request.getUserId());
            
            profile = new UserGameProfile(request.getUserId(), request.getProfileName());
        }
        
        // 모든 필드 업데이트
        if (request.getLevel() != null) profile.setLevel(request.getLevel());
        if (request.getExperience() != null) profile.setExperience(request.getExperience());
        if (request.getGold() != null) profile.setGold(request.getGold());
        if (request.getEquippedItems() != null) profile.setEquippedItems(request.getEquippedItems());
        if (request.getSkillInfo() != null) profile.setSkillInfo(request.getSkillInfo());
        profile.setUpdatedAt(LocalDateTime.now());
        
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        return new UserGameProfileResponse(
            savedProfile.getId(),    // Entity는 id
            savedProfile.getUserId(),
            savedProfile.getProfileName(),
            savedProfile.getLevel(),
            savedProfile.getExperience(),
            savedProfile.getGold(),
            savedProfile.getEquippedItems(),
            savedProfile.getSkillInfo(),
            savedProfile.getCreatedAt(),
            savedProfile.getUpdatedAt()
        );
    }
    
    /**
     * UserGameProfile의 개별 속성을 업데이트합니다.
     * null이 아닌 필드만 업데이트합니다.
     * @param request 업데이트할 정보 (profileId와 null이 아닌 필드만 업데이트)
     * @return 업데이트된 UserGameProfileResponse (프로필이 없으면 null)
     */
    @Transactional
    public UserGameProfileResponse updateUserGameProfile(UserGameProfileUpdateRequest request) {
        Optional<UserGameProfile> profileOptional = userGameProfileRepository.findById(request.getProfileId());
        
        if (profileOptional.isEmpty()) {
            return null;
        }
        
        UserGameProfile profile = profileOptional.get();
        
        // 소유권 검증
        validateProfileOwnership(profile);
        
        // null이 아닌 필드만 업데이트
        if (request.getLevel() != null) {
            profile.setLevel(request.getLevel());
        }
        if (request.getExperience() != null) {
            profile.setExperience(request.getExperience());
        }
        if (request.getGold() != null) {
            profile.setGold(request.getGold());
        }
        if (request.getEquippedItems() != null) {
            profile.setEquippedItems(request.getEquippedItems());
        }
        if (request.getSkillInfo() != null) {
            profile.setSkillInfo(request.getSkillInfo());
        }
        
        profile.setUpdatedAt(LocalDateTime.now());
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        return new UserGameProfileResponse(
            savedProfile.getId(),    // Entity는 id
            savedProfile.getUserId(),
            savedProfile.getProfileName(),
            savedProfile.getLevel(),
            savedProfile.getExperience(),
            savedProfile.getGold(),
            savedProfile.getEquippedItems(),
            savedProfile.getSkillInfo(),
            savedProfile.getCreatedAt(),
            savedProfile.getUpdatedAt()
        );
    }
    
    /**
     * Gold를 증감합니다. (최적화된 단일 필드 업데이트)
     * 음수 amount로 차감도 가능합니다.
     * 낙관적 락으로 동시성 문제 해결
     * @param request profileId와 증감량
     * @return 간소화된 Gold 업데이트 응답 (프로필이 없으면 null)
     */
    @Transactional
    public GoldUpdateResponse updateGold(GoldUpdateRequest request) {
        UserGameProfile profile = userGameProfileRepository
            .findById(request.getProfileId())
            .orElse(null);
        
        if (profile == null) {
            return null;
        }
        
        // 소유권 검증
        validateProfileOwnership(profile);
        
        // Gold 증감 (음수면 차감)
        int newGold = profile.getGold() + request.getAmount();
        
        // Gold가 음수가 되지 않도록 검증
        if (newGold < 0) {
            throw new IllegalArgumentException("Gold cannot be negative. Current: " + profile.getGold() + ", Amount: " + request.getAmount());
        }
        
        profile.setGold(newGold);
        profile.setUpdatedAt(LocalDateTime.now());
        
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        // 간소화된 응답 (변경된 정보만)
        return new GoldUpdateResponse(
            savedProfile.getId(),    // Entity는 id
            savedProfile.getGold(),
            request.getAmount(),
            savedProfile.getUpdatedAt()
        );
    }
    
    /**
     * Experience를 증가시킵니다. (레벨업 로직 제외)
     * 낙관적 락으로 동시성 문제 해결
     * @param request profileId와 획득 경험치
     * @return ExperienceUpdateResponse (레벨과 경험치 정보)
     */
    @Transactional
    public ExperienceUpdateResponse addExperience(ExperienceUpdateRequest request) {
        UserGameProfile profile = userGameProfileRepository
            .findById(request.getProfileId())
            .orElse(null);
        
        if (profile == null) {
            return null;
        }
        
        // 소유권 검증
        validateProfileOwnership(profile);
        
        // 경험치 증가만 처리 (레벨업은 별도 처리)
        long newExperience = profile.getExperience() + request.getAmount();
        
        profile.setExperience(newExperience);
        profile.setUpdatedAt(LocalDateTime.now());
        
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        // 간소화된 응답 (변경된 정보만)
        return new ExperienceUpdateResponse(
            savedProfile.getId(),    // Entity는 id
            savedProfile.getLevel(),
            savedProfile.getExperience(),
            request.getAmount(),
            savedProfile.getUpdatedAt()
        );
    }
    
    /**
     * 장비 슬롯에 아이템을 장착하거나 해제합니다. (최적화된 단일 슬롯 업데이트)
     * 낙관적 락으로 동시성 문제 해결
     * @param request profileId, 슬롯명, 아이템ID (null이면 해제)
     * @return 간소화된 장비 업데이트 응답 (프로필이 없으면 null)
     */
    @Transactional
    public EquipmentUpdateResponse updateEquipmentSlot(EquipmentSlotRequest request) {
        UserGameProfile profile = userGameProfileRepository
            .findById(request.getProfileId())
            .orElse(null);
        
        if (profile == null) {
            return null;
        }
        
        // 소유권 검증
        validateProfileOwnership(profile);
        
        // 이전 아이템 ID 저장
        Long previousItemId = profile.getEquippedItem(request.getSlot());
        
        // 슬롯 장착/해제
        if (request.getItemId() == null) {
            // 해제
            profile.unequipItem(request.getSlot());
        } else {
            // 장착
            profile.equipItem(request.getSlot(), request.getItemId());
        }
        
        profile.setUpdatedAt(LocalDateTime.now());
        UserGameProfile savedProfile = userGameProfileRepository.save(profile);
        
        // 간소화된 응답 (변경된 슬롯 정보만)
        return new EquipmentUpdateResponse(
            savedProfile.getId(),    // Entity는 id
            request.getSlot(),
            request.getItemId(),
            previousItemId,
            savedProfile.getUpdatedAt()
        );
    }
    
} 