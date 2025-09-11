package com.example.toremainserver.dto;

import java.util.List;

public class NftListClientResponse {
    
    private boolean success;
    private String errorMessage;
    private List<ItemData> itemDataList;
    
    // 기본 생성자
    public NftListClientResponse() {}
    
    // 성공 응답 생성자
    public NftListClientResponse(boolean success, List<ItemData> itemDataList) {
        this.success = success;
        this.itemDataList = itemDataList;
    }
    
    // 실패 응답 생성자
    public NftListClientResponse(boolean success, String errorMessage) {
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
    
    public List<ItemData> getItemDataList() {
        return itemDataList;
    }
    
    public void setItemDataList(List<ItemData> itemDataList) {
        this.itemDataList = itemDataList;
    }
}
