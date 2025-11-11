package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class NftUnlockUpRequest {
    
    @NotNull(message = "사용자 ID는 필수입니다")
    @Positive(message = "사용자 ID는 양수여야 합니다")
    private Long userId;
    
    @NotBlank(message = "NFT ID(토큰 ID)는 필수입니다")
    private String nftId;
    
    // 기본 생성자
    public NftUnlockUpRequest() {}
    
    // 생성자
    public NftUnlockUpRequest(Long userId, String nftId) {
        this.userId = userId;
        this.nftId = nftId;
    }
    
    // Getter와 Setter
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getNftId() {
        return nftId;
    }
    
    public void setNftId(String nftId) {
        this.nftId = nftId;
    }
}

