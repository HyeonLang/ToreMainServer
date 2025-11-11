package com.example.toremainserver.dto.nft;

public class NftLockUpResponse {
    
    private boolean success;
    private String errorMessage;
    
    // 기본 생성자
    public NftLockUpResponse() {}
    
    // 생성자
    public NftLockUpResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // 성공 응답 생성 메서드
    public static NftLockUpResponse success() {
        return new NftLockUpResponse(true, null);
    }
    
    // 실패 응답 생성 메서드
    public static NftLockUpResponse failure(String errorMessage) {
        return new NftLockUpResponse(false, errorMessage);
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
}

