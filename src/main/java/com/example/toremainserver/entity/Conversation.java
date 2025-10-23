package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Entity
@Table(name = "conversations")
public class Conversation {
    
    // 대화 기록을 위한 내부 클래스
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
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "conversation_id")
    private Long conversationId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "npc_id", nullable = false)
    private Long npcId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recent_history", columnDefinition = "JSON")
    private List<ChatHistory> recentHistory; // 최근 20턴의 대화 기록
    
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary; // 20턴 이전 대화의 요약본
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated; // 마지막 대화 시간
    
    // 기본 생성자
    public Conversation() {
        this.lastUpdated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
    
    // 생성자
    public Conversation(Long userId, Long npcId) {
        this.userId = userId;
        this.npcId = npcId;
        this.lastUpdated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
    
    public Conversation(Long userId, Long npcId, List<ChatHistory> recentHistory, String summary) {
        this.userId = userId;
        this.npcId = npcId;
        this.recentHistory = recentHistory;
        this.summary = summary;
        this.lastUpdated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
    
    // Getter와 Setter
    public Long getConversationId() {
        return conversationId;
    }
    
    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public List<ChatHistory> getRecentHistory() {
        return recentHistory;
    }
    
    public void setRecentHistory(List<ChatHistory> recentHistory) {
        this.recentHistory = recentHistory;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    // 대화 갱신 헬퍼 메서드
    public void updateConversation(List<ChatHistory> newHistory, String newSummary) {
        this.recentHistory = newHistory;
        this.summary = newSummary;
        this.lastUpdated = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}

