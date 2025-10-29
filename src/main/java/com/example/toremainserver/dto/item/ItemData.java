package com.example.toremainserver.dto.item;

import java.util.Map;

public class ItemData {
    
    private Long itemDefId;  // ItemDefinition.id 참조
    private Map<String, Object> enhancementData;
    
    // 기본 생성자
    public ItemData() {}
    
    // 생성자
    public ItemData(Long itemDefId, Map<String, Object> enhancementData) {
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
    }
    
    // Getter와 Setter
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
