package com.example.toremainserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NftMintClientRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
    
    @NotNull(message = "아이템 ID는 필수입니다")
    @Positive(message = "아이템 ID는 양수여야 합니다")
    private Integer itemId;
    
    @NotNull(message = "사용자 장비 아이템 ID는 필수입니다")
    @Positive(message = "사용자 장비 아이템 ID는 양수여야 합니다")
    private Long userEquipItemId;
    
    // 기본 생성자
    public NftMintClientRequest() {}
    
    // 생성자
    public NftMintClientRequest(Long userId, Integer itemId, Long userEquipItemId) {
        this.userId = userId;
        this.itemId = itemId;
        this.userEquipItemId = userEquipItemId;
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
    
    public Long getUserEquipItemId() {
        return userEquipItemId;
    }
    
    public void setUserEquipItemId(Long userEquipItemId) {
        this.userEquipItemId = userEquipItemId;
    }
}
