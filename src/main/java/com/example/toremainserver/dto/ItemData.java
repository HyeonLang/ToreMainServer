package com.example.toremainserver.dto;

import java.util.Map;

public class ItemData {
    
    private Integer itemId;
    private Map<String, Object> enhancementData;
    
    // 기본 생성자
    public ItemData() {}
    
    // 생성자
    public ItemData(Integer itemId, Map<String, Object> enhancementData) {
        this.itemId = itemId;
        this.enhancementData = enhancementData;
    }
    
    // Getter와 Setter
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
}
