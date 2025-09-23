package com.example.toremainserver.dto.market;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateSellOrderRequest {
    
    @NotBlank(message = "판매자 주소는 필수입니다")
    private String seller;
    
    @NotBlank(message = "NFT 컨트랙트 주소는 필수입니다")
    private String nftContract;
    
    @NotBlank(message = "토큰 ID는 필수입니다")
    private String tokenId;
    
    @NotBlank(message = "가격은 필수입니다")
    private String price;
    
    @NotBlank(message = "통화는 필수입니다")
    private String currency;
    
    @NotNull(message = "nonce는 필수입니다")
    @Positive(message = "nonce는 양수여야 합니다")
    private Long nonce;
    
    @NotNull(message = "만료 시간은 필수입니다")
    @Positive(message = "만료 시간은 양수여야 합니다")
    private Long deadline;
    
    @NotBlank(message = "서명은 필수입니다")
    private String signature;
    
    // 기본 생성자
    public CreateSellOrderRequest() {}
    
    // 생성자
    public CreateSellOrderRequest(String seller, String nftContract, String tokenId, 
                                 String price, String currency, Long nonce, 
                                 Long deadline, String signature) {
        this.seller = seller;
        this.nftContract = nftContract;
        this.tokenId = tokenId;
        this.price = price;
        this.currency = currency;
        this.nonce = nonce;
        this.deadline = deadline;
        this.signature = signature;
    }
    
    // Getter와 Setter
    public String getSeller() {
        return seller;
    }
    
    public void setSeller(String seller) {
        this.seller = seller;
    }
    
    public String getNftContract() {
        return nftContract;
    }
    
    public void setNftContract(String nftContract) {
        this.nftContract = nftContract;
    }
    
    public String getTokenId() {
        return tokenId;
    }
    
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }
    
    public String getPrice() {
        return price;
    }
    
    public void setPrice(String price) {
        this.price = price;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public Long getNonce() {
        return nonce;
    }
    
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }
    
    public Long getDeadline() {
        return deadline;
    }
    
    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }
    
    public String getSignature() {
        return signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
