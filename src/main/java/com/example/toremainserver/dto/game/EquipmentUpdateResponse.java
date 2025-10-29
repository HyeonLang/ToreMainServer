package com.example.toremainserver.dto.game;

import java.time.LocalDateTime;

public class EquipmentUpdateResponse {
    private Long profileId;       // userId 대신 profileId
    private String slot;          // 변경된 슬롯명
    private Long itemId;          // 장착된 아이템 ID (null이면 해제)
    private Long previousItemId;  // 이전 아이템 ID
    private LocalDateTime updatedAt;

    public EquipmentUpdateResponse() {
    }

    public EquipmentUpdateResponse(Long profileId, String slot, Long itemId, 
                                  Long previousItemId, LocalDateTime updatedAt) {
        this.profileId = profileId;
        this.slot = slot;
        this.itemId = itemId;
        this.previousItemId = previousItemId;
        this.updatedAt = updatedAt;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getPreviousItemId() {
        return previousItemId;
    }

    public void setPreviousItemId(Long previousItemId) {
        this.previousItemId = previousItemId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

