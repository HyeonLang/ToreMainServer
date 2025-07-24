package com.example.toremainserver.controller;

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
     *   "npcId": 1,
     *   "history": ["안녕", "무엇을 도와줄까?"]
     * }
     *
     * 엔드포인트: POST /api/npc
     *
     * 내부 처리 흐름:
     * 1. UE5에서 npcId, history를 POST로 받음
     * 2. (TODO) 필요한 정보(prompt 등) 가공
     * 3. /api.ai/npc로 POST 요청
     * 4. 응답을 UE5로 그대로 반환
     */
    @PostMapping("/npc")
    public ResponseEntity<?> ue5Npc(@RequestBody Map<String, Object> body) {
        // TODO: UE5에서 받은 정보를 가공하거나 추가 처리
        return gameEventService.forwardNpcRequest(body);
    }

    /**
     * UE5 서버에서 질감/색 문장 요청을 받으면 내부적으로 /api.ai/material로 POST 요청을 보내고,
     * 응답을 다시 UE5 서버로 반환합니다.
     *
     * 요청 body 예시:
     * {
     *   "description": "거친 돌 질감, 파란색"
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