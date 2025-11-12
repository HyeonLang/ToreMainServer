package com.example.toremainserver.repository;

import com.example.toremainserver.entity.NFTMarketOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NFTMarketOrderRepository extends JpaRepository<NFTMarketOrder, Long> {
    
    // 주문 ID로 조회
    Optional<NFTMarketOrder> findByOrderId(String orderId);
    
    // 판매자별 주문 조회
    List<NFTMarketOrder> findBySeller(String seller);
    
    // 상태별 주문 조회
    List<NFTMarketOrder> findByStatus(NFTMarketOrder.OrderStatus status);
    
    // 활성 주문만 조회
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> findActiveOrders();
    
    // 상태별 주문 조회 (UserEquipItem과 ItemDefinition 포함) - N+1 문제 방지
    @Query("SELECT s, uei, id FROM NFTMarketOrder s " +
           "LEFT JOIN UserEquipItem uei ON uei.nftId = s.tokenId " +
           "LEFT JOIN ItemDefinition id ON id.id = uei.itemDefId " +
           "WHERE s.status IN :statuses ORDER BY s.createdAt DESC")
    List<Object[]> findOrdersWithItemInfoByStatuses(@Param("statuses") List<String> statuses);
    
    // 활성 주문 개수 조회 (성능 최적화)
    @Query("SELECT COUNT(s) FROM NFTMarketOrder s WHERE s.status = 'ACTIVE'")
    long countActiveOrders();
    
    // 판매자와 상태로 조회
    List<NFTMarketOrder> findBySellerAndStatus(String seller, NFTMarketOrder.OrderStatus status);
    
    // 판매자의 활성 주문 조회
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.seller = :seller AND s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> findActiveOrdersBySeller(@Param("seller") String seller);
    
    // 판매자의 완료된 주문 조회 (SOLD, SELLER_CANCELLED)
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.seller = :seller AND s.status IN ('SOLD', 'SELLER_CANCELLED') ORDER BY s.updatedAt DESC")
    List<NFTMarketOrder> findCompletedOrdersBySeller(@Param("seller") String seller);
    
    // 판매자의 주문 통계
    @Query("SELECT s.status, COUNT(s) FROM NFTMarketOrder s WHERE s.seller = :seller GROUP BY s.status")
    List<Object[]> getOrderStatsBySeller(@Param("seller") String seller);
    
    // NFT 컨트랙트와 토큰 ID로 조회
    List<NFTMarketOrder> findByNftContractAndTokenId(String nftContract, String tokenId);
    
    // 토큰 ID로 조회
    List<NFTMarketOrder> findByTokenId(String tokenId);
    
    // 통화별 조회
    List<NFTMarketOrder> findByStatusAndCurrency(NFTMarketOrder.OrderStatus status, String currency);
    
    // 만료된 주문 조회
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.status = 'EXPIRED'")
    List<NFTMarketOrder> findExpiredOrders(@Param("currentTime") Long currentTime);
    
    // 락된 주문 조회 (lockedBy 필드가 제거되었으므로 이 쿼리는 사용되지 않을 수 있음)
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.status = :status")
    List<NFTMarketOrder> findByStatusAndLockedBy(@Param("status") NFTMarketOrder.OrderStatus status, @Param("lockedBy") String lockedBy);
    
    // 통계용 쿼리
    @Query("SELECT COUNT(s) FROM NFTMarketOrder s WHERE s.status = :status")
    long countByStatus(@Param("status") NFTMarketOrder.OrderStatus status);
    
    @Query("SELECT SUM(CAST(s.price AS java.math.BigInteger)) FROM NFTMarketOrder s WHERE s.status = 'SOLD'")
    String getTotalVolume();
    
    @Query("SELECT AVG(CAST(s.price AS java.math.BigInteger)) FROM NFTMarketOrder s WHERE s.status = 'SOLD'")
    String getAveragePrice();
    
    // 검색용 쿼리 (활성 주문만, 아이템 이름으로 검색)
    @Query("SELECT DISTINCT s FROM NFTMarketOrder s " +
           "LEFT JOIN UserEquipItem uei ON uei.nftId = s.tokenId " +
           "LEFT JOIN ItemDefinition id ON id.id = uei.itemDefId " +
           "WHERE s.status = 'ACTIVE' AND " +
           "(:query = '' OR id.name LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> searchActiveOrdersByItemName(@Param("query") String query);
    
    // 상태별 주문 조회 (아이템 이름으로 검색)
    @Query("SELECT DISTINCT s FROM NFTMarketOrder s " +
           "LEFT JOIN UserEquipItem uei ON uei.nftId = s.tokenId " +
           "LEFT JOIN ItemDefinition id ON id.id = uei.itemDefId " +
           "WHERE s.status = :status AND " +
           "(:query = '' OR id.name LIKE CONCAT('%', :query, '%')) " +
           "ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> searchOrdersByStatusAndItemName(@Param("status") NFTMarketOrder.OrderStatus status, @Param("query") String query);
    
    // 검색용 쿼리 (활성 주문만) - 기존 메서드 (하위 호환성)
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.status = 'ACTIVE' AND " +
           "(s.tokenId LIKE %:query% OR s.nftContract LIKE %:query%) ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> searchActiveOrdersByQuery(@Param("query") String query);
    
    // 페이징을 위한 활성 주문 조회
    @Query("SELECT s FROM NFTMarketOrder s WHERE s.status = 'ACTIVE' ORDER BY s.createdAt DESC")
    List<NFTMarketOrder> findActiveOrdersWithPaging(@Param("offset") int offset, @Param("limit") int limit);
}

