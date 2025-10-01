package com.example.toremainserver.dto.item;

import java.util.Map;

public class EquipItemRequest {
    private Long userId;
    private Integer itemId;
    private Long localItemId;
    private Map<String, Object> enhancementData;
    
    // 기본 생성자
    public EquipItemRequest() {}
    
    // 생성자
    public EquipItemRequest(Long userId, Integer itemId, Long localItemId, Map<String, Object> enhancementData) {
        this.userId = userId;
        this.itemId = itemId;
        this.localItemId = localItemId;
        this.enhancementData = enhancementData;
    }
    
    // Getter와 Setter
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
    
    public Long getLocalItemId() {
        return localItemId;
    }
    
    public void setLocalItemId(Long localItemId) {
        this.localItemId = localItemId;
    }
    
    public Map<String, Object> getEnhancementData() {
        return enhancementData;
    }
    
    public void setEnhancementData(Map<String, Object> enhancementData) {
        this.enhancementData = enhancementData;
    }
}
