package com.example.toremainserver.dto.game;

import java.util.List;
import java.util.Map;

public class Ue5NpcRequest {
    
    // 이전 대화 기록을 위한 내부 클래스
    public static class ChatHistory {
        private String speaker; // "player" 또는 "npc"
        private String message;
        private String timestamp;
        
        public ChatHistory() {}
        
        public ChatHistory(String speaker, String message, String timestamp) {
            this.speaker = speaker;
            this.message = message;
            this.timestamp = timestamp;
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
    }
    
    // UE5에서 받는 필드들
    private Long npcId;
    private Long profileId; // 프로필 ID 추가
    private ChatHistory currentPlayerMessage;
    private List<ChatHistory> previousChatHistory;
    private Map<String, Object> playerDescription; // 플레이어 설명 (JSON)
    private String apiKey; // API 키
    
    // 이전 대화 요약 정보 (20턴 이상의 대화를 요약한 문자열)
    private String previousConversationSummary;
    
    // 기본 생성자
    public Ue5NpcRequest() {}
    
    // 생성자
    public Ue5NpcRequest(Long npcId, Long profileId,
                         ChatHistory currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription) {
        this.npcId = npcId;
        this.profileId = profileId;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
    }
    
    // 생성자 (previousConversationSummary 포함)
    public Ue5NpcRequest(Long npcId, Long profileId,
                         ChatHistory currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription, String previousConversationSummary) {
        this.npcId = npcId;
        this.profileId = profileId;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
        this.previousConversationSummary = previousConversationSummary;
    }
    
    // 생성자 (apiKey 포함)
    public Ue5NpcRequest(Long npcId, Long profileId,
                         ChatHistory currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription, String previousConversationSummary, String apiKey) {
        this.npcId = npcId;
        this.profileId = profileId;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
        this.previousConversationSummary = previousConversationSummary;
        this.apiKey = apiKey;
    }
    
    // Getter와 Setter
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }
    
    public ChatHistory getCurrentPlayerMessage() {
        return currentPlayerMessage;
    }
    
    public void setCurrentPlayerMessage(ChatHistory currentPlayerMessage) {
        this.currentPlayerMessage = currentPlayerMessage;
    }
    
    public List<ChatHistory> getPreviousChatHistory() {
        return previousChatHistory;
    }
    
    public void setPreviousChatHistory(List<ChatHistory> previousChatHistory) {
        this.previousChatHistory = previousChatHistory;
    }
    
    public Map<String, Object> getPlayerDescription() {
        return playerDescription;
    }
    
    public void setPlayerDescription(Map<String, Object> playerDescription) {
        this.playerDescription = playerDescription;
    }

    public String getPreviousConversationSummary() {
        return previousConversationSummary;
    }

    public void setPreviousConversationSummary(String previousConversationSummary) {
        this.previousConversationSummary = previousConversationSummary;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
