package com.example.toremainserver.controller;

import com.example.toremainserver.dto.game.NpcChatResponse;
import com.example.toremainserver.dto.game.Ue5NpcRequest;
import com.example.toremainserver.service.GameEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
     * 엔드포인트: POST /api/npc
     *
     * 내부 처리 흐름:
     * 1. UE5에서 Ue5NpcRequest 정보를 POST로 받음
     * 2. DB에서 NPC 정보 조회 (추후 구현)
     * 3. 완전한 NpcChatRequest 구성 (previousConversationSummary 포함)
     * 4. /api.ai/npc로 POST 요청
     * 5. 응답을 UE5로 그대로 반환 (previousConversationSummary 포함)
     */
    @PostMapping("/npc")
    public ResponseEntity<NpcChatResponse> ue5Npc(@RequestBody Ue5NpcRequest ue5Request) {
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
} 