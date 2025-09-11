package com.example.toremainserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NftTransferClientRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
    
    @NotNull(message = "아이템 ID는 필수입니다")
    @Positive(message = "아이템 ID는 양수여야 합니다")
    private Integer itemId;
    
    @NotNull(message = "사용자 장비 아이템 ID는 필수입니다")
    @Positive(message = "사용자 장비 아이템 ID는 양수여야 합니다")
    private Long userEquipItemId;
    
    @NotNull(message = "NFT ID는 필수입니다")
    @Positive(message = "NFT ID는 양수여야 합니다")
    private Long nftId;
    
    @NotNull(message = "받는 사용자 ID는 필수입니다")
    @Positive(message = "받는 사용자 ID는 양수여야 합니다")
    private Long toUserId;
    
    // 기본 생성자
    public NftTransferClientRequest() {}
    
    // 생성자
    public NftTransferClientRequest(Long userId, Integer itemId, Long userEquipItemId, Long nftId, Long toUserId) {
        this.userId = userId;
        this.itemId = itemId;
        this.userEquipItemId = userEquipItemId;
        this.nftId = nftId;
        this.toUserId = toUserId;
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
    
    public Long getNftId() {
        return nftId;
    }
    
    public void setNftId(Long nftId) {
        this.nftId = nftId;
    }
    
    public Long getToUserId() {
        return toUserId;
    }
    
    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }
}
