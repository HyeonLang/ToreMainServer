package com.example.toremainserver.dto.nft;

public class ContractNftResponse {
    
    private boolean success;
    private String errorMessage;
    private String nftId;
    
    // 기본 생성자
    public ContractNftResponse() {}
    
    // 모든 필드를 받는 생성자
    public ContractNftResponse(boolean success, String nftId, String errorMessage) {
        this.success = success;
        this.nftId = nftId;
        this.errorMessage = errorMessage;
    }
    
    // 성공 응답 생성 메서드
    public static ContractNftResponse success(String nftId) {
        return new ContractNftResponse(true, nftId, null);
    }
    
    // 실패 응답 생성 메서드
    public static ContractNftResponse failure(String errorMessage) {
        return new ContractNftResponse(false, null, errorMessage);
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
