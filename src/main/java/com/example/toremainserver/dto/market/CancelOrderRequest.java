package com.example.toremainserver.dto.market;

import jakarta.validation.constraints.NotBlank;

public class CancelOrderRequest {
    
    @NotBlank(message = "사용자 주소는 필수입니다")
    private String userAddress;
    
    // 기본 생성자
    public CancelOrderRequest() {}
    
    // 생성자
    public CancelOrderRequest(String userAddress) {
        this.userAddress = userAddress;
    }
    
    // Getter와 Setter
    public String getUserAddress() {
        return userAddress;
    }
    
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
}
