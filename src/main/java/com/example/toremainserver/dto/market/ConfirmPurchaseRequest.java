package com.example.toremainserver.dto.market;

import jakarta.validation.constraints.NotBlank;

public class ConfirmPurchaseRequest {
    
    @NotBlank(message = "구매자 주소는 필수입니다")
    private String buyerAddress;
    
    @NotBlank(message = "트랜잭션 해시는 필수입니다")
    private String txHash;
    
    // 기본 생성자
    public ConfirmPurchaseRequest() {}
    
    // 생성자
    public ConfirmPurchaseRequest(String buyerAddress, String txHash) {
        this.buyerAddress = buyerAddress;
        this.txHash = txHash;
    }
    
    // Getter와 Setter
    public String getBuyerAddress() {
        return buyerAddress;
    }
    
    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }
    
    public String getTxHash() {
        return txHash;
    }
    
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
