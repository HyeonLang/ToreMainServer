package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "npcs")
public class Npc {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "npc_id")
    private Long npcId;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "npc_info", columnDefinition = "JSON")
    private Map<String, Object> npcInfo; // JSON을 Map으로 저장
    
    // 기본 생성자
    public Npc() {}
    
    // 생성자
    public Npc(String name, Map<String, Object> npcInfo) {
        this.name = name;
        this.npcInfo = npcInfo;
    }
    
    public Npc(Long npcId, String name, Map<String, Object> npcInfo) {
        this.npcId = npcId;
        this.name = name;
        this.npcInfo = npcInfo;
    }
    
    // Getter와 Setter
    public Long getNpcId() {
        return npcId;
    }
    
    public void setNpcId(Long npcId) {
        this.npcId = npcId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Map<String, Object> getNpcInfo() {
        return npcInfo;
    }
    
    public void setNpcInfo(Map<String, Object> npcInfo) {
        this.npcInfo = npcInfo;
    }
} 