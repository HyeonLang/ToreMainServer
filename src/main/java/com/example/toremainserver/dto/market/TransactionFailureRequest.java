package com.example.toremainserver.dto.market;

import jakarta.validation.constraints.NotBlank;

public class TransactionFailureRequest {
    
    @NotBlank(message = "구매자 주소는 필수입니다")
    private String buyerAddress;
    
    @NotBlank(message = "오류 메시지는 필수입니다")
    private String errorMessage;
    
    // 기본 생성자
    public TransactionFailureRequest() {}
    
    // 생성자
    public TransactionFailureRequest(String buyerAddress, String errorMessage) {
        this.buyerAddress = buyerAddress;
        this.errorMessage = errorMessage;
    }
    
    // Getter와 Setter
    public String getBuyerAddress() {
        return buyerAddress;
    }
    
    public void setBuyerAddress(String buyerAddress) {
        this.buyerAddress = buyerAddress;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
