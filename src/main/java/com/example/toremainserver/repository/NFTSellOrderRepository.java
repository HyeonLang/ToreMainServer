package com.example.toremainserver.repository;

import com.example.toremainserver.entity.NFTSellOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NFTSellOrderRepository extends JpaRepository<NFTSellOrder, Long> {
    
    // 주문 ID로 조회
    Optional<NFTSellOrder> findByOrderId(String orderId);
    
    // 판매자별 주문 조회
    List<NFTSellOrder> findBySeller(String seller);
    
    // 상태별 주문 조회
    List<NFTSellOrder> findByStatus(NFTSellOrder.OrderStatus status);
    
    // 판매자와 상태로 조회
    List<NFTSellOrder> findBySellerAndStatus(String seller, NFTSellOrder.OrderStatus status);
    
    // NFT 컨트랙트와 토큰 ID로 조회
    List<NFTSellOrder> findByNftContractAndTokenId(String nftContract, String tokenId);
    
    // 가격 범위로 조회
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status = :status AND " +
           "CAST(s.price AS java.math.BigInteger) BETWEEN :minPrice AND :maxPrice")
    List<NFTSellOrder> findByPriceRange(@Param("status") NFTSellOrder.OrderStatus status,
                                       @Param("minPrice") String minPrice,
                                       @Param("maxPrice") String maxPrice);
    
    // 통화별 조회
    List<NFTSellOrder> findByStatusAndCurrency(NFTSellOrder.OrderStatus status, String currency);
    
    // 만료된 주문 조회
    @Query("SELECT s FROM NFTSellOrder s WHERE s.deadline < :currentTime AND s.status = 'ACTIVE'")
    List<NFTSellOrder> findExpiredOrders(@Param("currentTime") Long currentTime);
    
    // 락된 주문 조회
    List<NFTSellOrder> findByStatusAndLockedBy(NFTSellOrder.OrderStatus status, String lockedBy);
    
    // 통계용 쿼리
    @Query("SELECT COUNT(s) FROM NFTSellOrder s WHERE s.status = :status")
    long countByStatus(@Param("status") NFTSellOrder.OrderStatus status);
    
    @Query("SELECT SUM(CAST(s.price AS java.math.BigInteger)) FROM NFTSellOrder s WHERE s.status = 'COMPLETED'")
    String getTotalVolume();
    
    @Query("SELECT AVG(CAST(s.price AS java.math.BigInteger)) FROM NFTSellOrder s WHERE s.status = 'COMPLETED'")
    String getAveragePrice();
    
    // 검색용 쿼리
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status = 'ACTIVE' AND " +
           "(s.tokenId LIKE %:query% OR s.nftContract LIKE %:query%)")
    List<NFTSellOrder> searchByQuery(@Param("query") String query);
    
    // 인기 NFT 조회 (조회수나 거래량 기준으로 정렬)
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<NFTSellOrder> findPopularNFTs();
}
