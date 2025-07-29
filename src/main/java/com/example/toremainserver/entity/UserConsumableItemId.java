package com.example.toremainserver.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserConsumableItemId implements Serializable {
    private Long userId;
    private Integer itemId;
    
    public UserConsumableItemId() {}
    
    public UserConsumableItemId(Long userId, Integer itemId) {
        this.userId = userId;
        this.itemId = itemId;
    }
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConsumableItemId that = (UserConsumableItemId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(itemId, that.itemId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(userId, itemId);
    }
} 