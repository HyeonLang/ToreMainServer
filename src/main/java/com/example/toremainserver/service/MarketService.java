package com.example.toremainserver.service;

import com.example.toremainserver.dto.market.MarketStatsResponse;
import com.example.toremainserver.entity.NFTMarketOrder;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.repository.NFTMarketOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarketService {
    private static final Logger logger = LoggerFactory.getLogger(MarketService.class);
    
    @Autowired
    private NFTMarketOrderRepository marketOrderRepository;
    
    
    // ==================== 판매 주문 관련 메서드 ====================
    
    /**
     * 활성 판매 주문 목록 조회
     * UserEquipItem과 ItemDefinition을 함께 조회하여 N+1 문제 방지
     * @param status 조회할 상태 (예: "active", "sold", "seller_cancelled", "expired", "all")
     *               "active": ACTIVE 상태만 조회
     *               기타: 해당 상태만 조회 (대소문자 무시)
     * @return NFTMarketOrder 리스트
     */
    public List<NFTMarketOrder> getActiveSellOrders(String status) {
        if (status == null || status.equalsIgnoreCase("active") || status.equalsIgnoreCase("all")) {
            // 기본값: ACTIVE 상태만 조회
            return marketOrderRepository.findActiveOrders();
        } else {
            // 특정 상태로 조회
            try {
                // 상태값을 enum으로 변환 (대소문자 무시)
                String normalizedStatus = status.toUpperCase().replace("-", "_");
                NFTMarketOrder.OrderStatus orderStatus = NFTMarketOrder.OrderStatus.valueOf(normalizedStatus);
                return marketOrderRepository.findByStatus(orderStatus);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값인 경우 기본값 사용
                return marketOrderRepository.findActiveOrders();
            }
        }
    }
    
    /**
     * 활성 주문 개수 조회 (성능 최적화)
     */
    public long getActiveOrdersCount() {
        return marketOrderRepository.countActiveOrders();
    }
    
    /**
     * 판매자의 활성 주문 조회
     */
    public List<NFTMarketOrder> getActiveOrdersBySeller(String seller) {
        return marketOrderRepository.findActiveOrdersBySeller(seller);
    }
    
    /**
     * 판매자의 완료된 주문 조회
     */
    public List<NFTMarketOrder> getCompletedOrdersBySeller(String seller) {
        return marketOrderRepository.findCompletedOrdersBySeller(seller);
    }
    
    /**
     * 판매자의 주문 통계 조회
     */
    public Map<String, Long> getOrderStatsBySeller(String seller) {
        List<Object[]> results = marketOrderRepository.getOrderStatsBySeller(seller);
        Map<String, Long> stats = new HashMap<>();
        
        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            stats.put(status, count);
        }
        
        return stats;
    }
    
    /**
     * 특정 사용자의 판매 주문 조회
     */
    public List<NFTMarketOrder> getUserSellOrders(String userAddress) {
        return marketOrderRepository.findBySeller(userAddress);
    }
    
    /**
     * 특정 판매 주문 조회
     */
    public Optional<NFTMarketOrder> getSellOrder(String orderId) {
        return marketOrderRepository.findByOrderId(orderId);
    }
    
    // ==================== 통계 및 분석 메서드 ====================
    
    /**
     * 마켓 통계 조회
     */
    public MarketStatsResponse getMarketStats() {
        long totalSellOrders = marketOrderRepository.countByStatus(NFTMarketOrder.OrderStatus.ACTIVE);
        
        String totalVolume = marketOrderRepository.getTotalVolume();
        String averagePrice = marketOrderRepository.getAveragePrice();
        
        return new MarketStatsResponse(
            (int) totalSellOrders,
            0, // 구매 주문 수는 0으로 설정
            totalVolume != null ? totalVolume : "0",
            averagePrice != null ? averagePrice : "0"
        );
    }
    
    // ==================== 검색 및 필터링 메서드 ====================
    
    /**
     * NFT 검색
     * @param query 아이템 이름 검색어 (빈 문자열이면 모든 아이템)
     * @param filters 필터 맵 (minPrice, maxPrice, category, status)
     */
    public List<NFTMarketOrder> searchNFTs(String query, Map<String, String> filters) {
        List<NFTMarketOrder> results;
        
        // query가 null이거나 빈 문자열이면 빈 문자열로 처리
        String searchQuery = (query == null || query.trim().isEmpty()) ? "" : query.trim();
        
        // status 필터가 있으면 해당 상태로 조회, 없으면 활성 주문만 조회
        if (filters != null && filters.containsKey("status")) {
            try {
                String statusStr = filters.get("status").toUpperCase().replace("-", "_");
                NFTMarketOrder.OrderStatus orderStatus = NFTMarketOrder.OrderStatus.valueOf(statusStr);
                // 상태별로 조회 (아이템 이름으로 검색)
                results = marketOrderRepository.searchOrdersByStatusAndItemName(orderStatus, searchQuery);
            } catch (IllegalArgumentException e) {
                // 잘못된 상태값인 경우 활성 주문만 조회
                results = marketOrderRepository.searchActiveOrdersByItemName(searchQuery);
            }
        } else {
            // 기본값: 활성 주문만 조회 (아이템 이름으로 검색)
            results = marketOrderRepository.searchActiveOrdersByItemName(searchQuery);
        }
        
        // 필터 적용
        if (filters != null) {
            // 가격 범위 필터
            if (filters.containsKey("minPrice") && filters.containsKey("maxPrice")) {
                results = results.stream()
                        .filter(order -> {
                            try {
                                long price = Long.parseLong(order.getPrice());
                                long minPrice = Long.parseLong(filters.get("minPrice"));
                                long maxPrice = Long.parseLong(filters.get("maxPrice"));
                                return price >= minPrice && price <= maxPrice;
                            } catch (NumberFormatException e) {
                                return false;
                            }
                        })
                        .collect(Collectors.toList());
            }
            
            // category 필터 (ItemDefinition의 category로 필터링)
            if (filters.containsKey("category")) {
                String categoryFilter = filters.get("category").toUpperCase();
                results = results.stream()
                        .filter(order -> {
                            // UserEquipItem과 ItemDefinition을 조인해서 category 확인
                            // 이 부분은 추가 쿼리가 필요하므로, 간단하게 처리
                            // 실제로는 Repository에서 조인 쿼리로 처리하는 것이 더 효율적
                            return true; // 일단 통과, 나중에 개선 가능
                        })
                        .collect(Collectors.toList());
            }
        }
        
        return results;
    }
    
    // ==================== 이벤트 리스너 메서드 ====================
    
    /**
     * MarketplaceVault 이벤트 리스너용: tokenId로 마켓플레이스 주문 갱신
     * @param tokenId NFT 토큰 ID
     * @param buyerWalletAddress 구매자 지갑 주소 (null 가능)
     * @return 갱신 성공 여부
     */
    public boolean updateMarketOrderByMarketplaceVaultEvent(String tokenId, String buyerWalletAddress) {
        // TODO: 구현 필요
        return false;
    }
}
