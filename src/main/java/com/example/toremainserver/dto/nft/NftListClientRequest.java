package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NftListClientRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
    
    // 기본 생성자
    public NftListClientRequest() {}
    
    // 생성자
    public NftListClientRequest(Long userId) {
        this.userId = userId;
    }
    
    // Getter와 Setter
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
