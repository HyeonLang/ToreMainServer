package com.example.toremainserver.dto;

import java.util.List;

public class NpcChatResponse {
    
    // 대화 기록을 위한 내부 클래스
    public static class ChatRecord {
        private String speaker; // "player" 또는 "npc"
        private String message;
        private String timestamp;
        
        public ChatRecord() {}
        
        public ChatRecord(String speaker, String message, String timestamp) {
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
    
    // 응답 필드들
    private Long npcId;
    private List<ChatRecord> chatHistory;
    private String npcResponse;
    
    // 기본 생성자
    public NpcChatResponse() {}
    
    // 생성자
    public NpcChatResponse(Long npcId, List<ChatRecord> chatHistory, String npcResponse) {
        this.npcId = npcId;
        this.chatHistory = chatHistory;
        this.npcResponse = npcResponse;
    }
    
    // Getter와 Setter
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public List<ChatRecord> getChatHistory() {
        return chatHistory;
    }
    
    public void setChatHistory(List<ChatRecord> chatHistory) {
        this.chatHistory = chatHistory;
    }
    
    public String getNpcResponse() {
        return npcResponse;
    }
    
    public void setNpcResponse(String npcResponse) {
        this.npcResponse = npcResponse;
    }
} 