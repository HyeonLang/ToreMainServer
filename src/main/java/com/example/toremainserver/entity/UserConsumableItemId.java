package com.example.toremainserver.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserConsumableItemId implements Serializable {
    private Long profileId;
    private Long itemDefId;
    
    public UserConsumableItemId() {}
    
    public UserConsumableItemId(Long profileId, Long itemDefId) {
        this.profileId = profileId;
        this.itemDefId = itemDefId;
    }
    
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserConsumableItemId that = (UserConsumableItemId) o;
        return Objects.equals(profileId, that.profileId) &&
               Objects.equals(itemDefId, that.itemDefId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(profileId, itemDefId);
    }
} 