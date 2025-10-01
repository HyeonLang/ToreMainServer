package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class NftBurnClientRequest {
    
    @NotBlank(message = "사용자 지갑 주소는 필수입니다")
    private String userAddress;
    
    @NotNull(message = "토큰 ID는 필수입니다")
    @Positive(message = "토큰 ID는 양수여야 합니다")
    private Long tokenId;
    
    @NotBlank(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    // 기본 생성자
    public NftBurnClientRequest() {}
    
    // 생성자
    public NftBurnClientRequest(String userAddress, Long tokenId, String contractAddress) {
        this.userAddress = userAddress;
        this.tokenId = tokenId;
        this.contractAddress = contractAddress;
    }
    
    // Getter와 Setter
    public String getUserAddress() {
        return userAddress;
    }
    
    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }
    
    public Long getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
