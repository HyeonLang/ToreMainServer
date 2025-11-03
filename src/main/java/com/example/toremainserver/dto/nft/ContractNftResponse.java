package com.example.toremainserver.dto.nft;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractNftResponse {
    
    // 성공 시 필드들
    private String txHash;
    private Long tokenId;
    private String tokenURI;
    private String contractAddress;
    private String mintedTo;
    private Long itemId;
    private Long userEquipItemId;
    private Boolean itemDataIncluded;
    
    // 기본 생성자
    public ContractNftResponse() {}
    
    // 성공 응답 생성자
    public ContractNftResponse(String txHash, Long tokenId, String tokenURI, 
                                String contractAddress, String mintedTo, 
                                Long itemId, Long userEquipItemId, Boolean itemDataIncluded) {
        this.txHash = txHash;
        this.tokenId = tokenId;
        this.tokenURI = tokenURI;
        this.contractAddress = contractAddress;
        this.mintedTo = mintedTo;
        this.itemId = itemId;
        this.userEquipItemId = userEquipItemId;
        this.itemDataIncluded = itemDataIncluded;
    }
    
    // 성공 여부 확인 (tokenId가 있으면 성공)
    public boolean isSuccess() {
        return tokenId != null;
    }
    
    // Getter와 Setter
    public String getTxHash() {
        return txHash;
    }
    
    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
    
    public Long getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }
    
    public String getTokenURI() {
        return tokenURI;
    }
    
    public void setTokenURI(String tokenURI) {
        this.tokenURI = tokenURI;
    }
    
    public String getContractAddress() {
        return contractAddress;
    }
    
    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }
    
    public String getMintedTo() {
        return mintedTo;
    }
    
    public void setMintedTo(String mintedTo) {
        this.mintedTo = mintedTo;
    }
    
    public Long getItemId() {
        return itemId;
    }
    
    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }
    
    public Long getUserEquipItemId() {
        return userEquipItemId;
    }
    
    public void setUserEquipItemId(Long userEquipItemId) {
        this.userEquipItemId = userEquipItemId;
    }
    
    public Boolean getItemDataIncluded() {
        return itemDataIncluded;
    }
    
    public void setItemDataIncluded(Boolean itemDataIncluded) {
        this.itemDataIncluded = itemDataIncluded;
    }
    
    // 하위 호환성을 위한 메서드 (기존 코드에서 사용 중)
    public String getNftId() {
        return tokenId != null ? tokenId.toString() : null;
    }
}
