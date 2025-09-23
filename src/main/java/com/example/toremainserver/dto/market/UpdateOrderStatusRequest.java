package com.example.toremainserver.dto.market;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequest {
    
    @NotBlank(message = "주문 ID는 필수입니다")
    private String orderId;
    
    @NotBlank(message = "상태는 필수입니다")
    private String status;
    
    private String buyer;
    private Long matchedAt;
    
    // 기본 생성자
    public UpdateOrderStatusRequest() {}
    
    // 생성자
    public UpdateOrderStatusRequest(String orderId, String status, String buyer, Long matchedAt) {
        this.orderId = orderId;
        this.status = status;
        this.buyer = buyer;
        this.matchedAt = matchedAt;
    }
    
    // Getter와 Setter
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getBuyer() {
        return buyer;
    }
    
    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }
    
    public Long getMatchedAt() {
        return matchedAt;
    }
    
    public void setMatchedAt(Long matchedAt) {
        this.matchedAt = matchedAt;
    }
}
