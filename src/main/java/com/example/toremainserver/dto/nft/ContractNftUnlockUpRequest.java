package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public class ContractNftUnlockUpRequest {
    
    @NotBlank(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    @NotBlank(message = "NFT ID(토큰 ID)는 필수입니다")
    private String tokenId;
    
    @NotBlank(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    // 기본 생성자
    public ContractNftUnlockUpRequest() {}
    
    // 생성자
    public ContractNftUnlockUpRequest(String walletAddress, String tokenId, String contractAddress) {
        this.walletAddress = walletAddress;
        this.tokenId = tokenId;
        this.contractAddress = contractAddress;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
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

