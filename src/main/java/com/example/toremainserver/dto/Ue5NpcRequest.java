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
} 