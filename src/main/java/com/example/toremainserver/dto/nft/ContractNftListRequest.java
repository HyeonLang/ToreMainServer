package com.example.toremainserver.dto.nft;

import jakarta.validation.constraints.NotNull;

public class ContractNftListRequest {
    
    @NotNull(message = "지갑 주소는 필수입니다")
    private String walletAddress;
    
    @NotNull(message = "컨트랙트 주소는 필수입니다")
    private String contractAddress;
    
    // 기본 생성자
    public ContractNftListRequest() {}
    
    // 생성자
    public ContractNftListRequest(String walletAddress, String contractAddress) {
        this.walletAddress = walletAddress;
        this.contractAddress = contractAddress;
    }
    
    // Getter와 Setter
    public String getWalletAddress() {
        return walletAddress;
    }
    
    public void setWalletAddress(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
