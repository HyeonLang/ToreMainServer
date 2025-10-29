package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ContractNftRequest {
    
    @NotNull(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    
    @NotNull(message = "아이템 정의 ID는 필수입니다")
    @Positive(message = "아이템 정의 ID는 양수여야 합니다")
    private Long itemDefId;  // ItemDefinition.id 참조
    
    @NotNull(message = "장비 아이템 ID는 필수입니다")
    @Positive(message = "장비 아이템 ID는 양수여야 합니다")
    private Long equipItemId;  // UserEquipItem.id 참조
    
    @NotNull(message = "아이템 데이터는 필수입니다")
    private Object itemData; // JSON 형태의 아이템 데이터
    
    // 기본 생성자
    public ContractNftRequest() {}
    
    // 생성자
    public ContractNftRequest(String walletAddress, Long itemDefId, Long equipItemId, Object itemData) {
        this.walletAddress = walletAddress;
        this.itemDefId = itemDefId;
        this.equipItemId = equipItemId;
        this.itemData = itemData;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    
    public Long getItemDefId() {
        return itemDefId;
    }
    
    public void setItemDefId(Long itemDefId) {
        this.itemDefId = itemDefId;
    }
    
    public Long getEquipItemId() {
        return equipItemId;
    }
    
    public void setEquipItemId(Long equipItemId) {
        this.equipItemId = equipItemId;
    }
    
    public Object getItemData() {
        return itemData;
    }
    
    public void setItemData(Object itemData) {
        this.itemData = itemData;
    }
}
