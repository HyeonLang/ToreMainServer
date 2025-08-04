package com.example.toremainserver.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "user_consumable_items")
@IdClass(UserConsumableItemId.class)
public class UserConsumableItem {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    
    @Id
    @Column(name = "item_id")
    private Integer itemId;
    
    @Column(name = "quantity", columnDefinition = "INT DEFAULT 0")
    private Integer quantity = 0;
    
    @Column(name = "local_item_id", nullable = false)
    private Long localItemId;
    
    // 기본 생성자
    public UserConsumableItem() {}
    
    // 생성자
    public UserConsumableItem(Long userId, Integer itemId, Integer quantity) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
    }
    
    public UserConsumableItem(Long userId, Integer itemId, Integer quantity, Long localItemId) {
        this.userId = userId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.localItemId = localItemId;
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
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public Long getLocalItemId() {
        return localItemId;
    }
    
    public void setLocalItemId(Long localItemId) {
        this.localItemId = localItemId;
    }
    
}

 