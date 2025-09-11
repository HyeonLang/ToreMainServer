package com.example.toremainserver.dto;

import java.util.List;

public class ContractNftListResponse {
    
    private boolean success;
    private String errorMessage;
    private List<Long> nftIdList;
    
    // 기본 생성자
    public ContractNftListResponse() {}
    
    // 성공 응답 생성자
    public ContractNftListResponse(boolean success, List<Long> nftIdList) {
        this.success = success;
        this.nftIdList = nftIdList;
    }
    
    // 실패 응답 생성자
    public ContractNftListResponse(boolean success, String errorMessage) {
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
    
    public List<Long> getNftIdList() {
        return nftIdList;
    }
    
    public void setNftIdList(List<Long> nftIdList) {
        this.nftIdList = nftIdList;
    }
}
