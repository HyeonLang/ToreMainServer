package com.example.toremainserver.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_consumable_items")
@IdClass(UserConsumableItemId.class)
public class UserConsumableItem {
    
    @Id
    @Column(name = "profile_id")
    private Long profileId;
    
    @Id
    @Column(name = "item_def_id")
    private Long itemDefId;  // ItemDefinition.id 참조 (복합키)
    
    @Column(name = "quantity", columnDefinition = "INT DEFAULT 0")
    private Integer quantity = 0;
    
    // 기본 생성자
    public UserConsumableItem() {}
    
    // 생성자
    public UserConsumableItem(Long profileId, Long itemDefId, Integer quantity) {
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

 