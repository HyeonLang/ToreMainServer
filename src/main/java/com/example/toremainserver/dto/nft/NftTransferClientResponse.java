package com.example.toremainserver.dto.nft;

public class NftTransferClientResponse {
    
    private boolean success;
    private String errorMessage;
    
    // 기본 생성자
    public NftTransferClientResponse() {}
    
    // 성공 응답 생성자
    public NftTransferClientResponse(boolean success) {
        this.success = success;
    }
    
    // 실패 응답 생성자
    public NftTransferClientResponse(boolean success, String errorMessage) {
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
}
