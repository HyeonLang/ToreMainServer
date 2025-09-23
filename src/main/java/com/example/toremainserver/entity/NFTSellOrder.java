package com.example.toremainserver.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nft_sell_orders")
public class NFTSellOrder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "order_id", unique = true, nullable = false)
    private String orderId;
    
    @Column(name = "seller", nullable = false)
    private String seller;
    
    @Column(name = "nft_contract", nullable = false)
    private String nftContract;
    
    @Column(name = "token_id", nullable = false)
    private String tokenId;
    
    @Column(name = "price", nullable = false)
    private String price;
    
    @Column(name = "currency", nullable = false)
    private String currency;
    
    @Column(name = "nonce", nullable = false)
    private Long nonce;
    
    @Column(name = "deadline", nullable = false)
    private Long deadline;
    
    @Column(name = "signature", nullable = false, columnDefinition = "TEXT")
    private String signature;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;
    
    @Column(name = "buyer")
    private String buyer;
    
    @Column(name = "matched_at")
    private Long matchedAt;
    
    @Column(name = "locked_by")
    private String lockedBy;
    
    @Column(name = "locked_at")
    private Long lockedAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public NFTSellOrder() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.ACTIVE;
    }
    
    // 생성자
    public NFTSellOrder(String orderId, String seller, String nftContract, String tokenId,
                       String price, String currency, Long nonce, Long deadline, String signature) {
        this();
        this.orderId = orderId;
        this.seller = seller;
        this.nftContract = nftContract;
        this.tokenId = tokenId;
        this.price = price;
        this.currency = currency;
        this.nonce = nonce;
        this.deadline = deadline;
        this.signature = signature;
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
    
    public String getLockedBy() {
        return lockedBy;
    }
    
    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }
    
    public Long getLockedAt() {
        return lockedAt;
    }
    
    public void setLockedAt(Long lockedAt) {
        this.lockedAt = lockedAt;
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
        ACTIVE,     // 활성 상태
        LOCKED,     // 락 상태 (구매 진행 중)
        COMPLETED,  // 완료
        CANCELLED   // 취소
    }
}
