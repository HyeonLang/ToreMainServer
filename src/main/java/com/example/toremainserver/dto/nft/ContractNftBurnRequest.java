package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ContractNftBurnRequest {
    
    @NotNull(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    
    @NotNull(message = "NFT ID는 필수입니다")
    @Positive(message = "NFT ID는 양수여야 합니다")
    private Long nftId;
    
    // 기본 생성자
    public ContractNftBurnRequest() {}
    
    // 생성자
    public ContractNftBurnRequest(String walletAddress, Long nftId) {
        this.walletAddress = walletAddress;
        this.nftId = nftId;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    
    public Long getNftId() {
        return nftId;
    }
    
    public void setNftId(Long nftId) {
        this.nftId = nftId;
    }
}
