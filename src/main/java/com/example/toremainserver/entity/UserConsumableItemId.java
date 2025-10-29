package com.example.toremainserver.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserConsumableItemId implements Serializable {
    private Long userId;
    private Long itemDefId;
    
    public UserConsumableItemId() {}
    
    public UserConsumableItemId(Long userId, Long itemDefId) {
        this.userId = userId;
        this.itemDefId = itemDefId;
    }
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConsumableItemId that = (UserConsumableItemId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(itemDefId, that.itemDefId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, itemDefId);
    }
} 