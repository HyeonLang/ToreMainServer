package com.example.toremainserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ContractNftRequest {
    
    @NotNull(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    @NotNull(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    @NotNull(message = "아이템 ID는 필수입니다")
    @Positive(message = "아이템 ID는 양수여야 합니다")
    private Integer itemId;
    
    @NotNull(message = "사용자 장비 아이템 ID는 필수입니다")
    @Positive(message = "사용자 장비 아이템 ID는 양수여야 합니다")
    private Long userEquipItemId;
    
    @NotNull(message = "아이템 데이터는 필수입니다")
    private Object itemData; // JSON 형태의 아이템 데이터
    
    // 기본 생성자
    public ContractNftRequest() {}
    
    // 생성자
    public ContractNftRequest(String walletAddress, String contractAddress, 
                               Integer itemId, Long userEquipItemId, Object itemData) {
        this.walletAddress = walletAddress;
        this.contractAddress = contractAddress;
        this.itemId = itemId;
        this.userEquipItemId = userEquipItemId;
        this.itemData = itemData;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
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
    
    public Object getItemData() {
        return itemData;
    }
    
    public void setItemData(Object itemData) {
        this.itemData = itemData;
    }
}
