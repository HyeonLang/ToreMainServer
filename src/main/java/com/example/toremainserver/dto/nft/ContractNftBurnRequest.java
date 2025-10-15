package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotBlank;

public class ContractNftBurnRequest {
    
    @NotBlank(message = "사용자 지갑 주소는 필수입니다")
    private String userAddress;
    
    @NotBlank(message = "토큰 ID는 필수입니다")
    private String tokenId;
    
    @NotBlank(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    // 기본 생성자
    public ContractNftBurnRequest() {}
    
    // 생성자
    public ContractNftBurnRequest(String userAddress, String tokenId, String contractAddress) {
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
    
    public String getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
