package com.example.toremainserver.dto.game;

public class EquipmentSlotRequest {
    private Long profileId;  // userId 대신 profileId
    private String slot;     // 장비 슬롯명 (예: "weapon", "armor", "helmet")
    private Long itemId;     // 장착할 아이템 ID (null이면 해제)

    public EquipmentSlotRequest() {
    }

    public EquipmentSlotRequest(Long profileId, String slot, Long itemId) {
        this.profileId = profileId;
        this.slot = slot;
        this.itemId = itemId;
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
}

