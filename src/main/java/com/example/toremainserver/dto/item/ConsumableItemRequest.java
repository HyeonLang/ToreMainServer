package com.example.toremainserver.dto.item;

import java.util.Map;

public class ConsumableItemRequest {
    private Long userId;
    private Integer itemId;
    private Long localItemId;
    private Integer quantity;


    // 기본 생성자
    public ConsumableItemRequest() {}

    // 생성자
    public ConsumableItemRequest(Long userId, Integer itemId, Long localItemId, Integer quantity) {
        this.userId = userId;
        this.itemId = itemId;
        this.localItemId = localItemId;
        this.quantity = quantity;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
