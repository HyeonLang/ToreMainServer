package com.example.toremainserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nft_market_orders")
public class NFTMarketOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 데이터베이스 기본키 (자동 증가)
    
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId; // 주문 고유 식별자 (UUID 등)
    
    @Column(name = "seller", nullable = false)
    private String seller; // 판매자 지갑 주소
    
    @Column(name = "nft_contract", nullable = false)
    private String nftContract; // NFT 컨트랙트 주소
    
    @Column(name = "token_id", nullable = false)
    private String tokenId; // NFT 토큰 ID
    
    @Column(name = "price", nullable = false)
    private String price; // 판매 가격 (Wei 단위, String으로 저장)
    
    @Column(name = "currency", nullable = false)
    private String currency; // 결제 통화 (예: ETH, USDT 등)
    
    @Column(name = "vault_address", nullable = false)
    private String vaultAddress; // 중앙 관리 vault 지갑 주소
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // 주문 상태 (Active, SellerCancelled, Expired, Sold)
    
    @Column(name = "buyer")
    private String buyer; // 구매자 지갑 주소 (매칭 시 설정)
    
    @Column(name = "matched_at")
    private Long matchedAt; // 매칭된 시간 (Unix 타임스탬프)
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt; // 주문 생성 시간
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 주문 수정 시간
    
    // 기본 생성자
    public NFTMarketOrder() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.ACTIVE;
    }
    
    // 생성자
    public NFTMarketOrder(String orderId, String seller, String nftContract, String tokenId,
                       String price, String currency, String vaultAddress) {
        this();
        this.orderId = orderId;
        this.seller = seller;
        this.nftContract = nftContract;
        this.tokenId = tokenId;
        this.price = price;
        this.currency = currency;
        this.vaultAddress = vaultAddress;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter와 Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
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
    
    public String getVaultAddress() {
        return vaultAddress;
    }
    
    public void setVaultAddress(String vaultAddress) {
        this.vaultAddress = vaultAddress;
    }
    
    public OrderStatus getStatus() {
        return status;
    }
    
    public void setStatus(OrderStatus status) {
        this.status = status;
    }
    
    public String getBuyer() {
        return buyer;
    }
    
    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }
    
    public Long getMatchedAt() {
        return matchedAt;
    }
    
    public void setMatchedAt(Long matchedAt) {
        this.matchedAt = matchedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // 주문 상태 열거형
    public enum OrderStatus {
        ACTIVE,             // 활성 상태
        SELLER_CANCELLED,   // 판매자 취소
        EXPIRED,            // 만료됨
        SOLD                // 판매 완료
    }
}

