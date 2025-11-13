package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotBlank;

public class NftLockUpRequest {
    
    @NotBlank(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    @NotBlank(message = "NFT ID(Token ID)는 필수입니다")
    private String nftId;
    
    // 기본 생성자
    public NftLockUpRequest() {}
    
    // 생성자
    public NftLockUpRequest(String walletAddress, String nftId) {
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
    
    public String getNftId() {
        return nftId;
    }
    
    public void setNftId(String nftId) {
        this.nftId = nftId;
    }
}

