package com.example.toremainserver.dto;

import java.util.List;

public class NpcChatRequest {
    
    // 대화 요청 종류 enum
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
    private ChatType chatType;
    private Long npcId;
    private String playerName;
    private String currentPlayerMessage;
    private List<ChatHistory> previousChatHistory;
    private String apiKey; // API 키
    
    // DB에서 가져올 필드들 (추후 구현)
    private String npcName;
    private String npcDescription;
    private String playerDescription;
    private String systemMessages;
    
    // 이전 대화 요약 정보 (20턴 이상의 대화를 요약한 문자열)
    private String previousConversationSummary;
    
    // 기본 생성자
    public NpcChatRequest() {}
    
    // UE5 요청용 생성자
    public NpcChatRequest(ChatType chatType, Long npcId, String playerName,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.playerName = playerName;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
    }
    
    // 완전한 요청용 생성자 (DB 정보 포함)
    public NpcChatRequest(ChatType chatType, Long npcId, String npcName, String playerName,
                         String npcDescription, String playerDescription, String systemMessages,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.npcName = npcName;
        this.playerName = playerName;
        this.npcDescription = npcDescription;
        this.playerDescription = playerDescription;
        this.systemMessages = systemMessages;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
    }
    
    // 완전한 요청용 생성자 (previousConversationSummary 포함)
    public NpcChatRequest(ChatType chatType, Long npcId, String npcName, String playerName,
                         String npcDescription, String playerDescription, String systemMessages,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         String previousConversationSummary) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.npcName = npcName;
        this.playerName = playerName;
        this.npcDescription = npcDescription;
        this.playerDescription = playerDescription;
        this.systemMessages = systemMessages;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
        this.previousConversationSummary = previousConversationSummary;
    }
    
    // 완전한 요청용 생성자 (apiKey 포함)
    public NpcChatRequest(ChatType chatType, Long npcId, String npcName, String playerName,
                         String npcDescription, String playerDescription, String systemMessages,
                         String currentPlayerMessage, List<ChatHistory> previousChatHistory,
                         String previousConversationSummary, String apiKey) {
        this.chatType = chatType;
        this.npcId = npcId;
        this.npcName = npcName;
        this.playerName = playerName;
        this.npcDescription = npcDescription;
        this.playerDescription = playerDescription;
        this.systemMessages = systemMessages;
        this.currentPlayerMessage = currentPlayerMessage;
        this.previousChatHistory = previousChatHistory;
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
    
    public String getNpcName() {
        return npcName;
    }
    
    public void setNpcName(String npcName) {
        this.npcName = npcName;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }
    
    public String getNpcDescription() {
        return npcDescription;
    }
    
    public void setNpcDescription(String npcDescription) {
        this.npcDescription = npcDescription;
    }
    
    public String getPlayerDescription() {
        return playerDescription;
    }
    
    public void setPlayerDescription(String playerDescription) {
        this.playerDescription = playerDescription;
    }
    
    public String getSystemMessages() {
        return systemMessages;
    }
    
    public void setSystemMessages(String systemMessages) {
        this.systemMessages = systemMessages;
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