package com.example.toremainserver.dto.item;

import java.util.Map;

public class EquipItemRequest {
    private Long profileId;
    private Long itemDefId;  // ItemDefinition.id 참조
    private Map<String, Object> enhancementData;
    
    // 기본 생성자
    public EquipItemRequest() {}
    
    // 생성자
    public EquipItemRequest(Long profileId, Long itemDefId, Map<String, Object> enhancementData) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.enhancementData = enhancementData;
    }
    
    // Getter와 Setter
    public Long getProfileId() {
        return profileId;
    }
    
    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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
