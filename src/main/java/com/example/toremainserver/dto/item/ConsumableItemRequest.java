package com.example.toremainserver.dto.item;

public class ConsumableItemRequest {
    private Long userId;
    private Long itemDefId;  // ItemDefinition.id 참조
    private Integer quantity;

    // 기본 생성자
    public ConsumableItemRequest() {}

    // 생성자
    public ConsumableItemRequest(Long userId, Long itemDefId, Integer quantity) {
        this.userId = userId;
        this.itemDefId = itemDefId;
        this.quantity = quantity;
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
    
    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
