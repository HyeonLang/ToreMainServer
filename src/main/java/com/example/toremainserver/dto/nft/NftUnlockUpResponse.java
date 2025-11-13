package com.example.toremainserver.dto.nft;

public class NftUnlockUpResponse {
    
    private boolean success;
    private String errorMessage;
    private String nftId;
    
    // 기본 생성자
    public NftUnlockUpResponse() {}
    
    // 생성자
    public NftUnlockUpResponse(boolean success, String errorMessage) {
        this.success = success;
        this.errorMessage = errorMessage;
    }
    
    // 생성자 (nftId 포함)
    public NftUnlockUpResponse(boolean success, String errorMessage, String nftId) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.nftId = nftId;
    }
    
    // 성공 응답 생성 메서드
    public static NftUnlockUpResponse success() {
        return new NftUnlockUpResponse(true, null, null);
    }
    
    // 성공 응답 생성 메서드 (nftId 포함)
    public static NftUnlockUpResponse success(String nftId) {
        return new NftUnlockUpResponse(true, null, nftId);
    }
    
    // 실패 응답 생성 메서드
    public static NftUnlockUpResponse failure(String errorMessage) {
        return new NftUnlockUpResponse(false, errorMessage, null);
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
    
    public String getNftId() {
        return nftId;
    }
    
    public void setNftId(String nftId) {
        this.nftId = nftId;
    }
}

