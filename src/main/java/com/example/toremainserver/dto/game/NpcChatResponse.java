package com.example.toremainserver.dto.game;

import java.util.Map;

public class NpcChatResponse {
    
    // 대화 기록을 위한 내부 클래스
    public static class ChatHistory {
        private String speaker; // "player" 또는 "npc"
        private String message;
        private String timestamp;
        private Map<String, Object> emotionData; // 감정 데이터 (JSON)
        
        public ChatHistory() {}
        
        public ChatHistory(String speaker, String message, String timestamp) {
            this.speaker = speaker;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public ChatHistory(String speaker, String message, String timestamp, Map<String, Object> emotionData) {
            this.speaker = speaker;
            this.message = message;
            this.timestamp = timestamp;
            this.emotionData = emotionData;
        }
        
        // Getter와 Setter
        public String getSpeaker() {
            return speaker;
        }
        
        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, Object> getEmotionData() {
            return emotionData;
        }
        
        public void setEmotionData(Map<String, Object> emotionData) {
            this.emotionData = emotionData;
        }
    }
    
    // 응답 필드들
    private Long npcId;
    private ChatHistory npcResponse;
    
    // 이전 대화 요약 정보 (AI 서버에서 받아온 요약 문자열)
    private String previousConversationSummary;
    
    // 기본 생성자
    public NpcChatResponse() {}
    
    // 생성자
    public NpcChatResponse(Long npcId, ChatHistory npcResponse) {
        this.npcId = npcId;
        this.npcResponse = npcResponse;
    }
    
    // 생성자 (previousConversationSummary 포함)
    public NpcChatResponse(Long npcId, ChatHistory npcResponse, String previousConversationSummary) {
        this.npcId = npcId;
        this.npcResponse = npcResponse;
        this.previousConversationSummary = previousConversationSummary;
    }
    
    // Getter와 Setter
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public ChatHistory getNpcResponse() {
        return npcResponse;
    }
    
    public void setNpcResponse(ChatHistory npcResponse) {
        this.npcResponse = npcResponse;
    }
    
    public String getPreviousConversationSummary() {
        return previousConversationSummary;
    }
    
    public void setPreviousConversationSummary(String previousConversationSummary) {
        this.previousConversationSummary = previousConversationSummary;
    }
}
