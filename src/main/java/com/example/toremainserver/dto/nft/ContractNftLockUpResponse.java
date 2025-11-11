package com.example.toremainserver.dto.nft;

public class ContractNftLockUpResponse {
    
    private boolean success;
    private String errorMessage;
    private String txHash;
    
    // 기본 생성자
    public ContractNftLockUpResponse() {}
    
    // 생성자
    public ContractNftLockUpResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // 성공 응답 생성자
    public ContractNftLockUpResponse(boolean success, String errorMessage, String txHash) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.txHash = txHash;
    }
    
    // Getter와 Setter
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getTxHash() {
        return txHash;
    }
    
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}

