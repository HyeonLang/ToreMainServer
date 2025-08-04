package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;

@Entity
@Table(name = "user_equip_items")
public class UserEquipItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "item_id", nullable = false)
    private Integer itemId;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "enhancement_data", columnDefinition = "JSON")
    private Map<String, Object> enhancementData;
    
    @Column(name = "nft_id", length = 100)
    private String nftId;
    
    @Column(name = "local_item_id", nullable = false)
    private Long localItemId;
    
    // 기본 생성자
    public UserEquipItem() {}
    
    // 생성자
    public UserEquipItem(Long userId, Integer itemId) {
        this.userId = userId;
        this.itemId = itemId;
    }
    
    public UserEquipItem(Long userId, Integer itemId, Map<String, Object> enhancementData) {
        this.userId = userId;
        this.itemId = itemId;
        this.enhancementData = enhancementData;
    }
    
    public UserEquipItem(Long userId, Integer itemId, Map<String, Object> enhancementData, String nftId) {
        this.userId = userId;
        this.itemId = itemId;
        this.enhancementData = enhancementData;
        this.nftId = nftId;
    }
    
    public UserEquipItem(Long userId, Integer itemId, Map<String, Object> enhancementData, String nftId, Long localItemId) {
        this.userId = userId;
        this.itemId = itemId;
        this.enhancementData = enhancementData;
        this.nftId = nftId;
        this.localItemId = localItemId;
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Integer getItemId() {
        return itemId;
    }
    
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }
    
    public Map<String, Object> getEnhancementData() {
        return enhancementData;
    }
    
    public void setEnhancementData(Map<String, Object> enhancementData) {
        this.enhancementData = enhancementData;
    }
    
    public String getNftId() {
        return nftId;
    }
    
    public void setNftId(String nftId) {
        this.nftId = nftId;
    }
    
    public Long getLocalItemId() {
        return localItemId;
    }
    
    public void setLocalItemId(Long localItemId) {
        this.localItemId = localItemId;
    }
} 