package com.example.toremainserver.controller;

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
import com.example.toremainserver.service.GameEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class GameEventController {
    private final GameEventService gameEventService;

    @Autowired
    public GameEventController(GameEventService gameEventService) {
        this.gameEventService = gameEventService;
    }

    /**
     * UE5 서버에서 NPC 대화 요청을 받으면 내부적으로 /api.ai/npc로 POST 요청을 보내고,
     * 응답을 다시 UE5 서버로 반환합니다.
     *
     * 요청 body 예시:
     * {
     *   "chatType": "START",
     *   "npcId": 1,
     *   "playerName": "모험가",
     *   "currentPlayerMessage": "안녕하세요",
     *   "previousChatHistory": [],
     *   "previousConversationSummary": "이전 대화 요약 정보 (20턴 이상의 대화를 요약한 문자열)",
     *   "playerDescription": {
     *     "class": "전사",
     *     "level": 10,
     *     "personality": "용감한",
     *     "background": "마을을 지키는 전사"
     *   },
     *   "api_key": "your_api_key_here"
     * }
     *
     * 응답 body 예시:
     * {
     *   "npcId": 1,
     *   "chatHistory": [...],
     *   "npcResponse": "NPC의 응답 메시지",
     *   "previousConversationSummary": "AI 서버에서 업데이트된 대화 요약 정보"
     * }
     *
     * 엔드포인트: POST /api/npc/chat
     *
     * 내부 처리 흐름:
     * 1. UE5에서 Ue5NpcRequest 정보를 POST로 받음
     * 2. DB에서 NPC 정보 조회 (추후 구현)
     * 3. 완전한 NpcChatRequest 구성 (previousConversationSummary 포함)
     * 4. /api.ai/npc로 POST 요청
     * 5. 응답을 UE5로 그대로 반환 (previousConversationSummary 포함)
     */
    @PostMapping("/npc/chat")
    public ResponseEntity<Ue5NpcResponse> ue5Npc(@RequestBody Ue5NpcRequest ue5Request) {
        return gameEventService.forwardNpcRequest(ue5Request);
    }

    /**
     * UE5 서버에서 질감/색 문장 요청을 받으면 내부적으로 /api.ai/material로 POST 요청을 보내고,
     * 응답을 다시 UE5 서버로 반환합니다.
     *
     * 요청 body 예시:
     * {
     *   "description": "거친 돌 질감, 파란색",
     *   "api_key": "your_api_key_here"
     * }
     *
     * 엔드포인트: POST /api/material
     *
     * 내부 처리 흐름:
     * 1. UE5에서 description을 POST로 받음
     * 2. (TODO) 필요한 정보 가공
     * 3. /api.ai/material로 POST 요청
     * 4. 응답(multipart/form-data)을 UE5로 그대로 반환
     */
    @PostMapping("/material")
    public ResponseEntity<?> ue5Material(@RequestBody Map<String, Object> body) {
        // TODO: UE5에서 받은 정보를 가공하거나 추가 처리
        return gameEventService.forwardMaterialRequest(body);
    }
    
    /**
     * userId와 npcId로 Conversation을 조회합니다.
     *
     * 요청 예시:
     * GET /api/npc/conversations?userId=123&npcId=5
     *
     * 응답 예시:
     * {
     *   "conversationId": 1,
     *   "userId": 123,
     *   "npcId": 5,
     *   "recentHistory": [
     *     {
     *       "speaker": "player",
     *       "message": "안녕하세요",
     *       "timestamp": "2025-10-20T10:00:00"
     *     },
     *     {
     *       "speaker": "npc",
     *       "message": "반갑네!",
     *       "timestamp": "2025-10-20T10:00:05"
     *     }
     *   ],
     *   "summary": "이전 대화 요약...",
     *   "lastUpdated": "2025-10-20T10:00:05"
     * }
     *
     * @param userId 유저 ID
     * @param npcId NPC ID
     * @param profileId 프로필 ID
     * @return Conversation (없으면 404)
     */
    @GetMapping("/npc/conversations")
    public ResponseEntity<Conversation> getNpcConversations(
            @RequestParam Long userId,
            @RequestParam Long profileId,
            @RequestParam Long npcId
    ) {
        Conversation conversation = gameEventService.getNpcConversations(userId, profileId, npcId);
        
        if (conversation == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * 모든 NPC 정보를 조회합니다.
     *
     * 요청 예시:
     * GET /api/npcs
     *
     * 응답 예시:
     * [
     *   {
     *     "npcId": 1,
     *     "name": "마을 촌장",
     *     "npcInfo": {
     *       "gender": "남성",
     *       "age": "60",
     *       "occupation": "촌장",
     *       "personality": "친절한",
     *       "persona": "마을을 오래 지켜온 촌장",
     *       "speakingStyle": "존댓말을 사용하며 차분한 말투"
     *     }
     *   },
     *   {
     *     "npcId": 2,
     *     "name": "대장간 주인",
     *     "npcInfo": {...}
     *   }
     * ]
     *
     * @return 모든 NPC 리스트
     */
    @GetMapping("/npcs")
    public ResponseEntity<List<Npc>> getAllNpcs() {
        List<Npc> npcs = gameEventService.getAllNpcs();
        return ResponseEntity.ok(npcs);
    }

    /**
     * 새 UserGameProfile을 생성합니다. (최초 1회 생성용)
     * 레벨, 경험치, 골드 등은 기본값으로 자동 초기화됩니다.
     *
     * 요청 예시:
     * POST /api/profile
     * {
     *   "userId": 1,
     *   "profileName": "메인 캐릭터"
     * }
     *
     * 응답 예시:
     * {
     *   "profileId": 12345,       // ← 생성된 프로필 ID (저장 필수!)
     *   "userId": 1,
     *   "profileName": "메인 캐릭터",
     *   "level": 1,               // 기본값
     *   "experience": 0,          // 기본값
     *   "gold": 0,                // 기본값
     *   "equippedItems": {},      // 빈 맵
     *   "skillInfo": {},          // 빈 맵
     *   "createdAt": "2025-10-29T17:00:00",
     *   "updatedAt": "2025-10-29T17:00:00"
     * }
     *
     * 에러 응답 (중복된 이름):
     * 400 Bad Request - "Profile with name '메인 캐릭터' already exists for this user"
     *
     * @param request userId와 profileName
     * @return 생성된 프로필 정보 (profileId 포함)
     */
    @PostMapping("/profile")
    public ResponseEntity<?> createProfile(@RequestBody ProfileCreateRequest request) {
        try {
            UserGameProfileResponse response = gameEventService.createUserGameProfile(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * userId로 해당 유저의 모든 프로필 목록을 조회합니다.
     * profileId와 profileName만 반환하여 캐릭터 선택 화면에 최적화되어 있습니다.
     *
     * 요청 예시:
     * GET /api/profiles?userId=1
     *
     * 응답 예시:
     * [
     *   { "profileId": 1, "profileName": "메인 캐릭터" },
     *   { "profileId": 2, "profileName": "서브 캐릭터" },
     *   { "profileId": 3, "profileName": "PVP 전용" }
     * ]
     *
     * 상세 정보가 필요하면 GET /api/profile?profileId={profileId} 사용
     *
     * @param userId 사용자 ID
     * @return 프로필 목록 (profileId, profileName만 포함)
     */
    @GetMapping("/profiles")
    public ResponseEntity<List<Map<String, Object>>> getUserGameProfiles(@RequestParam Long userId) {
        List<Map<String, Object>> profiles = gameEventService.getUserGameProfiles(userId);
        return ResponseEntity.ok(profiles);
    }
    
    /**
     * profileId로 UserGameProfile을 조회합니다.
     *
     * 요청 예시:
     * GET /api/profile?profileId=12345
     *
     * 응답 예시:
     * {
     *   "profileId": 12345,
     *   "userId": 1,
     *   "profileName": "메인 캐릭터",
     *   "level": 10,
     *   "experience": 5000,
     *   "gold": 1500,
     *   "equippedItems": {
     *     "weapon": 101,
     *     "armor": 201
     *   },
     *   "skillInfo": {
     *     "fireball": 3,
     *     "heal": 2
     *   },
     *   "createdAt": "2025-10-20T10:00:00",
     *   "updatedAt": "2025-10-25T15:30:00"
     * }
     *
     * @param profileId 프로필 ID
     * @return UserGameProfile 정보 (없으면 404)
     */
    @GetMapping("/profile")
    public ResponseEntity<UserGameProfileResponse> getUserGameProfile(@RequestParam Long profileId) {
        UserGameProfileResponse response = gameEventService.getUserGameProfile(profileId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * UserGameProfile의 모든 정보를 동기화합니다.
     * profileId가 있으면 기존 프로필을 업데이트하고, 없으면 새로 생성합니다.
     *
     * 요청 예시 (신규 생성):
     * PUT /api/profile/sync
     * {
     *   "userId": 1,
     *   "profileName": "메인 캐릭터",
     *   "level": 1,
     *   "experience": 0,
     *   "gold": 0,
     *   "equippedItems": {},
     *   "skillInfo": {}
     * }
     *
     * 요청 예시 (기존 프로필 업데이트):
     * PUT /api/profile/sync
     * {
     *   "profileId": 12345,
     *   "level": 15,
     *   "experience": 8000,
     *   "gold": 2500,
     *   "equippedItems": {
     *     "weapon": 102,
     *     "armor": 202,
     *     "accessory": 301
     *   },
     *   "skillInfo": {
     *     "fireball": 5,
     *     "heal": 3,
     *     "shield": 1
     *   }
     * }
     *
     * 응답 예시:
     * {
     *   "profileId": 12345,
     *   "userId": 1,
     *   "profileName": "메인 캐릭터",
     *   "level": 15,
     *   "experience": 8000,
     *   "gold": 2500,
     *   "equippedItems": {
     *     "weapon": 102,
     *     "armor": 202,
     *     "accessory": 301
     *   },
     *   "skillInfo": {
     *     "fireball": 5,
     *     "heal": 3,
     *     "shield": 1
     *   },
     *   "createdAt": "2025-10-20T10:00:00",
     *   "updatedAt": "2025-10-29T16:00:00"
     * }
     *
     * @param request 동기화할 전체 정보
     * @return 저장된 UserGameProfile 정보
     */
    @PutMapping("/profile/sync")
    public ResponseEntity<?> syncUserGameProfile(@RequestBody UserGameProfileSyncRequest request) {
        try {
            UserGameProfileResponse response = gameEventService.syncUserGameProfile(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * UserGameProfile의 개별 속성을 업데이트합니다.
     * null이 아닌 필드만 업데이트됩니다.
     *
     * 요청 예시 1 (레벨과 경험치만 업데이트):
     * PATCH /api/profile
     * {
     *   "profileId": 12345,
     *   "level": 11,
     *   "experience": 5500
     * }
     *
     * 요청 예시 2 (골드만 업데이트):
     * PATCH /api/profile
     * {
     *   "profileId": 12345,
     *   "gold": 2000
     * }
     *
     * 요청 예시 3 (장착 아이템만 업데이트):
     * PATCH /api/profile
     * {
     *   "profileId": 12345,
     *   "equippedItems": {
     *     "weapon": 105,
     *     "armor": 205
     *   }
     * }
     *
     * 응답 예시:
     * {
     *   "profileId": 12345,
     *   "userId": 1,
     *   "profileName": "메인 캐릭터",
     *   "level": 11,
     *   "experience": 5500,
     *   "gold": 1500,
     *   "equippedItems": {
     *     "weapon": 101,
     *     "armor": 201
     *   },
     *   "skillInfo": {
     *     "fireball": 3,
     *     "heal": 2
     *   },
     *   "createdAt": "2025-10-20T10:00:00",
     *   "updatedAt": "2025-10-29T16:05:00"
     * }
     *
     * @param request 업데이트할 정보 (null이 아닌 필드만 업데이트)
     * @return 업데이트된 UserGameProfile 정보 (프로필이 없으면 404)
     */
    @PatchMapping("/profile")
    public ResponseEntity<UserGameProfileResponse> updateUserGameProfile(@RequestBody UserGameProfileUpdateRequest request) {
        UserGameProfileResponse response = gameEventService.updateUserGameProfile(request);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gold를 증감합니다. (최적화된 전용 API)
     * 몬스터 처치, 아이템 판매/구매, 퀘스트 보상 등에 사용됩니다.
     *
     * 요청 예시 1 (Gold 증가 - 몬스터 처치):
     * PATCH /api/profile/gold
     * {
     *   "profileId": 12345,
     *   "amount": 500
     * }
     *
     * 요청 예시 2 (Gold 차감 - 아이템 구매):
     * PATCH /api/profile/gold
     * {
     *   "profileId": 12345,
     *   "amount": -300
     * }
     *
     * 응답 예시 (간소화된 응답):
     * {
     *   "profileId": 12345,
     *   "gold": 2200,
     *   "delta": 500,
     *   "updatedAt": "2025-10-29T16:10:00"
     * }
     *
     * 에러 응답 (Gold 부족):
     * 400 Bad Request - "Gold cannot be negative. Current: 200, Amount: -300"
     *
     * @param request profileId와 증감량 (음수면 차감)
     * @return 간소화된 Gold 업데이트 정보 (프로필이 없으면 404)
     */
    @PatchMapping("/profile/gold")
    public ResponseEntity<?> updateGold(@RequestBody GoldUpdateRequest request) {
        try {
            GoldUpdateResponse response = gameEventService.updateGold(request);
            
            if (response == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // Gold 부족 등의 검증 에러
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Experience를 증가시킵니다. (최적화된 전용 API)
     * 몬스터 처치, 퀘스트 완료, 던전 클리어 등에 사용됩니다.
     * 레벨업은 별도 API로 처리합니다.
     *
     * 요청 예시 (경험치 획득 - 몬스터 처치):
     * PATCH /api/profile/experience
     * {
     *   "profileId": 12345,
     *   "amount": 1500
     * }
     *
     * 응답 예시 (간소화된 응답):
     * {
     *   "profileId": 12345,
     *   "level": 10,
     *   "experience": 7500,
     *   "delta": 1500,
     *   "updatedAt": "2025-10-29T16:20:00"
     * }
     *
     * @param request profileId와 획득 경험치
     * @return 간소화된 경험치 업데이트 정보 (프로필이 없으면 404)
     */
    @PatchMapping("/profile/experience")
    public ResponseEntity<ExperienceUpdateResponse> addExperience(@RequestBody ExperienceUpdateRequest request) {
        ExperienceUpdateResponse response = gameEventService.addExperience(request);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 장비 슬롯에 아이템을 장착하거나 해제합니다. (최적화된 전용 API)
     * 단일 슬롯만 변경하므로 전체 Map을 전송하는 것보다 효율적입니다.
     *
     * 요청 예시 1 (무기 장착):
     * PATCH /api/profile/equipment
     * {
     *   "profileId": 12345,
     *   "slot": "weapon",
     *   "itemId": 105
     * }
     *
     * 요청 예시 2 (방어구 해제):
     * PATCH /api/profile/equipment
     * {
     *   "profileId": 12345,
     *   "slot": "armor",
     *   "itemId": null
     * }
     *
     * 응답 예시 (간소화된 응답):
     * {
     *   "profileId": 12345,
     *   "slot": "weapon",
     *   "itemId": 105,
     *   "previousItemId": 101,
     *   "updatedAt": "2025-10-29T16:15:00"
     * }
     *
     * @param request profileId, 슬롯명, 아이템ID (null이면 해제)
     * @return 간소화된 장비 업데이트 정보 (프로필이 없으면 404)
     */
    @PatchMapping("/profile/equipment")
    public ResponseEntity<EquipmentUpdateResponse> updateEquipmentSlot(@RequestBody EquipmentSlotRequest request) {
        EquipmentUpdateResponse response = gameEventService.updateEquipmentSlot(request);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
} 