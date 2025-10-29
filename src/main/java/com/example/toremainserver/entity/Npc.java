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
    @Column(name = "npc_id")  // DB 컬럼명은 명확하게
    private Long id;           // Java 필드명은 간결하게
    
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
    
    public Npc(Long id, String name, Map<String, Object> npcInfo) {
        this.id = id;
        this.name = name;
        this.npcInfo = npcInfo;
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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