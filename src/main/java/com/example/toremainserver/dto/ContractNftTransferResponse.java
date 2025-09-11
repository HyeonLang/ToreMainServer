package com.example.toremainserver.dto;

public class ContractNftTransferResponse {
    
    private boolean success;
    private String errorMessage;
    private String txHash;
    
    // 기본 생성자
    public ContractNftTransferResponse() {}
    
    // 성공 응답 생성자
    public ContractNftTransferResponse(boolean success, String txHash) {
        this.success = success;
        this.txHash = txHash;
    }
    
    // 실패 응답 생성자
    public ContractNftTransferResponse(boolean success, String errorMessage, boolean isError) {
        this.success = success;
        this.errorMessage = errorMessage;
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
