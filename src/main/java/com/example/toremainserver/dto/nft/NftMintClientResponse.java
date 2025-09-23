package com.example.toremainserver.dto.nft;

public class NftMintClientResponse {
    
    private boolean success;
    private String errorMessage;
    private Long nftId;
    
    // 기본 생성자
    public NftMintClientResponse() {}
    
    // 성공 응답 생성자
    public NftMintClientResponse(boolean success, Long nftId) {
        this.success = success;
        this.nftId = nftId;
    }
    
    // 실패 응답 생성자
    public NftMintClientResponse(boolean success, String errorMessage) {
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
    
    public Long getNftId() {
        return nftId;
    }
    
    public void setNftId(Long nftId) {
        this.nftId = nftId;
    }
}
