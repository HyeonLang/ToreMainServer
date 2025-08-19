package com.example.toremainserver.dto;

import java.util.List;
import java.util.Map;

public class Ue5NpcRequest {
    
    // 대화 요청 종류 enum (NpcChatRequest와 동일)
    public enum ChatType {
        START("시작"),
        CONTINUE("대화중"),
        END("종료");
        
        private final String value;
        
        ChatType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // 이전 대화 기록을 위한 내부 클래스 (NpcChatRequest와 동일)
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
    private ChatType chatType;
    private Long npcId;
    private String playerName;
    private String currentPlayerMessage;
    private List<ChatHistory> previousChatHistory;
    private Map<String, Object> playerDescription; // 플레이어 설명 (JSON)
    private String apiKey; // API 키
    
    // 이전 대화 요약 정보 (20턴 이상의 대화를 요약한 문자열)
    private String previousConversationSummary;
    
    // 기본 생성자
    public Ue5NpcRequest() {}
    
    // 생성자
    public Ue5NpcRequest(ChatType chatType, Long npcId, String playerName,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.playerName = playerName;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
    }
    
    // 생성자 (previousConversationSummary 포함)
    public Ue5NpcRequest(ChatType chatType, Long npcId, String playerName,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription, String previousConversationSummary) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.playerName = playerName;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
        this.previousConversationSummary = previousConversationSummary;
    }
    
    // 생성자 (apiKey 포함)
    public Ue5NpcRequest(ChatType chatType, Long npcId, String playerName,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         Map<String, Object> playerDescription, String previousConversationSummary, String apiKey) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.playerName = playerName;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.playerDescription = playerDescription;
        this.previousConversationSummary = previousConversationSummary;
        this.apiKey = apiKey;
    }
    
    // Getter와 Setter
    public ChatType getChatType() {
        return chatType;
    }
    
    public void setChatType(ChatType chatType) {
        this.chatType = chatType;
    }
    
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getCurrentPlayerMessage() {
        return currentPlayerMessage;
    }
    
    public void setCurrentPlayerMessage(String currentPlayerMessage) {
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