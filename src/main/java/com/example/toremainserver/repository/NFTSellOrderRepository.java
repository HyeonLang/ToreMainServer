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
    
    // 활성 주문만 조회 (ACTIVE + LOCKED)
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status IN ('ACTIVE', 'LOCKED') ORDER BY s.createdAt DESC")
    List<NFTSellOrder> findActiveOrders();
    
    // 상태별 주문 조회 (UserEquipItem과 ItemDefinition 포함) - N+1 문제 방지
    @Query("SELECT s, uei, id FROM NFTSellOrder s " +
           "LEFT JOIN UserEquipItem uei ON uei.nftId = s.tokenId " +
           "LEFT JOIN ItemDefinition id ON id.id = uei.itemDefId " +
           "WHERE s.status IN :statuses ORDER BY s.createdAt DESC")
    List<Object[]> findOrdersWithItemInfoByStatuses(@Param("statuses") List<String> statuses);
    
    // 활성 주문 개수 조회 (성능 최적화)
    @Query("SELECT COUNT(s) FROM NFTSellOrder s WHERE s.status IN ('ACTIVE', 'LOCKED')")
    long countActiveOrders();
    
    // 판매자와 상태로 조회
    List<NFTSellOrder> findBySellerAndStatus(String seller, NFTSellOrder.OrderStatus status);
    
    // 판매자의 활성 주문 조회
    @Query("SELECT s FROM NFTSellOrder s WHERE s.seller = :seller AND s.status IN ('ACTIVE', 'LOCKED') ORDER BY s.createdAt DESC")
    List<NFTSellOrder> findActiveOrdersBySeller(@Param("seller") String seller);
    
    // 판매자의 완료된 주문 조회 (COMPLETED, CANCELLED)
    @Query("SELECT s FROM NFTSellOrder s WHERE s.seller = :seller AND s.status IN ('COMPLETED', 'CANCELLED') ORDER BY s.updatedAt DESC")
    List<NFTSellOrder> findCompletedOrdersBySeller(@Param("seller") String seller);
    
    // 판매자의 주문 통계
    @Query("SELECT s.status, COUNT(s) FROM NFTSellOrder s WHERE s.seller = :seller GROUP BY s.status")
    List<Object[]> getOrderStatsBySeller(@Param("seller") String seller);
    
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
    
    // 검색용 쿼리 (활성 주문만)
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status IN ('ACTIVE', 'LOCKED') AND " +
           "(s.tokenId LIKE %:query% OR s.nftContract LIKE %:query%) ORDER BY s.createdAt DESC")
    List<NFTSellOrder> searchActiveOrdersByQuery(@Param("query") String query);
    
    // 인기 NFT 조회 (활성 주문만)
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status IN ('ACTIVE', 'LOCKED') ORDER BY s.createdAt DESC")
    List<NFTSellOrder> findPopularActiveNFTs();
    
    // 페이징을 위한 활성 주문 조회
    @Query("SELECT s FROM NFTSellOrder s WHERE s.status IN ('ACTIVE', 'LOCKED') ORDER BY s.createdAt DESC")
    List<NFTSellOrder> findActiveOrdersWithPaging(@Param("offset") int offset, @Param("limit") int limit);
}
