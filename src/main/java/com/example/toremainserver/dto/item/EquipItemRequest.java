package com.example.toremainserver.dto.item;

import java.util.Map;

public class EquipItemRequest {
    private Long userId;
    private Long itemDefId;  // ItemDefinition.id 참조
    private Map<String, Object> enhancementData;
    
    // 기본 생성자
    public EquipItemRequest() {}
    
    // 생성자
    public EquipItemRequest(Long userId, Long itemDefId, Map<String, Object> enhancementData) {
        this.userId = userId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
    }
    
    // Getter와 Setter
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getItemDefId() {
        return itemDefId;
    }
    
    public void setItemDefId(Long itemDefId) {
        this.itemDefId = itemDefId;
    }
    
    public Map<String, Object> getEnhancementData() {
        return enhancementData;
    }
    
    public void setEnhancementData(Map<String, Object> enhancementData) {
        this.enhancementData = enhancementData;
    }
}
