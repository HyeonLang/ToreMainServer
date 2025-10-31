package com.example.toremainserver.dto.item;

public class ConsumableItemRequest {
    private Long profileId;
    private Long itemDefId;  // ItemDefinition.id 참조
    private Integer quantity;

    // 기본 생성자
    public ConsumableItemRequest() {}

    // 생성자
    public ConsumableItemRequest(Long profileId, Long itemDefId, Integer quantity) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
        this.quantity = quantity;
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
    
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
