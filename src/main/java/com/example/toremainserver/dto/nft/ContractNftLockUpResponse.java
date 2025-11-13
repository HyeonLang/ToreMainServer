package com.example.toremainserver.dto.nft;

import java.util.Map;

public class ContractNftLockUpResponse {
    
    // 성공 응답 필드
    private String txHash;
    private String vaultAddress;
    private String message;
    
    // 실패 응답 필드
    private String error;
    private Map<String, Object> details;
    
    // 기본 생성자
    public ContractNftLockUpResponse() {}
    
    // 생성자 (하위 호환성 유지)
    public ContractNftLockUpResponse(boolean success, String errorMessage) {
        if (success) {
            this.message = errorMessage;
        } else {
            this.error = errorMessage;
        }
    }
    
    // 성공 여부 확인 (error 필드가 없으면 성공)
    public boolean isSuccess() {
        return error == null || error.isEmpty();
    }
    
    // 에러 메시지 반환
    public String getErrorMessage() {
        return error;
    }
    
    // Getter와 Setter
    public String getTxHash() {
        return txHash;
    }
    
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
    
    public String getVaultAddress() {
        return vaultAddress;
    }
    
    public void setVaultAddress(String vaultAddress) {
        this.vaultAddress = vaultAddress;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
}

