package com.example.toremainserver.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class ContractNftTransferRequest {
    
    @NotNull(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    @NotNull(message = "받는 지갑 주소는 필수입니다")
    private String toWalletAddress;
    
    @NotNull(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    @NotNull(message = "NFT ID는 필수입니다")
    @Positive(message = "NFT ID는 양수여야 합니다")
    private Long nftId;
    
    // 기본 생성자
    public ContractNftTransferRequest() {}
    
    // 생성자
    public ContractNftTransferRequest(String walletAddress, String toWalletAddress, String contractAddress, Long nftId) {
        this.walletAddress = walletAddress;
        this.toWalletAddress = toWalletAddress;
        this.contractAddress = contractAddress;
        this.nftId = nftId;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public String getToWalletAddress() {
        return toWalletAddress;
    }
    
    public void setToWalletAddress(String toWalletAddress) {
        this.toWalletAddress = toWalletAddress;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
    
    public Long getNftId() {
        return nftId;
    }
    
    public void setNftId(Long nftId) {
        this.nftId = nftId;
    }
}
