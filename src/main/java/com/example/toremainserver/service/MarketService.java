package com.example.toremainserver.service;

import com.example.toremainserver.dto.market.*;
import com.example.toremainserver.entity.NFTSellOrder;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.repository.NFTSellOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class MarketService {
    
    @Autowired
    private NFTSellOrderRepository sellOrderRepository;
    
    
    // ==================== 판매 주문 관련 메서드 ====================
    
    /**
     * 판매 주문 생성
     */
    public NFTSellOrder createSellOrder(CreateSellOrderRequest request) {
        // 주문 ID 생성 (UUID 사용)
        String orderId = UUID.randomUUID().toString();
        
        NFTSellOrder sellOrder = new NFTSellOrder(
            orderId,
            request.getSeller(),
            request.getNftContract(),
            request.getTokenId(),
            request.getPrice(),
            request.getCurrency(),
            request.getNonce(),
            request.getDeadline(),
            request.getSignature()
        );
        
        return sellOrderRepository.save(sellOrder);
    }
    
    /**
     * 활성 판매 주문 목록 조회
     * UserEquipItem과 ItemDefinition을 함께 조회하여 N+1 문제 방지
     * @param status 조회할 상태 (예: "active", "locked", "completed", "cancelled", "all")
     *               "active": ACTIVE + LOCKED 상태만 조회
     *               기타: 해당 상태만 조회 (대소문자 무시)
     * @return ActiveSellOrderResponse 리스트 (sellOrder, equipItem, itemDefinition 포함)
     */
    public List<ActiveSellOrderResponse> getActiveSellOrders(String status) {
        List<String> statuses;
        
        if (status == null || status.equalsIgnoreCase("active") || status.equalsIgnoreCase("all")) {
            // 기본값: ACTIVE + LOCKED
            statuses = Arrays.asList("ACTIVE", "LOCKED");
        } else {
            // 특정 상태로 조회
            String upperStatus = status.toUpperCase();
            if (NFTSellOrder.OrderStatus.ACTIVE.name().equals(upperStatus) ||
                NFTSellOrder.OrderStatus.LOCKED.name().equals(upperStatus) ||
                NFTSellOrder.OrderStatus.COMPLETED.name().equals(upperStatus) ||
                NFTSellOrder.OrderStatus.CANCELLED.name().equals(upperStatus)) {
                statuses = Arrays.asList(upperStatus);
            } else {
                // 잘못된 상태값인 경우 기본값 사용
                statuses = Arrays.asList("ACTIVE", "LOCKED");
            }
        }
        
        List<Object[]> results = sellOrderRepository.findOrdersWithItemInfoByStatuses(statuses);
        
        // 결과를 ActiveSellOrderResponse 리스트로 변환
        // Object[] 배열: [NFTSellOrder, UserEquipItem, ItemDefinition]
        return results.stream()
            .map(result -> {
                NFTSellOrder sellOrder = (NFTSellOrder) result[0];
                UserEquipItem equipItem = (UserEquipItem) result[1];  // null 가능
                ItemDefinition itemDefinition = (ItemDefinition) result[2];  // null 가능
                
                return new ActiveSellOrderResponse(sellOrder, equipItem, itemDefinition);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 활성 주문 개수 조회 (성능 최적화)
     */
    public long getActiveOrdersCount() {
        return sellOrderRepository.countActiveOrders();
    }
    
    /**
     * 판매자의 활성 주문 조회
     */
    public List<NFTSellOrder> getActiveOrdersBySeller(String seller) {
        return sellOrderRepository.findActiveOrdersBySeller(seller);
    }
    
    /**
     * 판매자의 완료된 주문 조회
     */
    public List<NFTSellOrder> getCompletedOrdersBySeller(String seller) {
        return sellOrderRepository.findCompletedOrdersBySeller(seller);
    }
    
    /**
     * 판매자의 주문 통계 조회
     */
    public Map<String, Long> getOrderStatsBySeller(String seller) {
        List<Object[]> results = sellOrderRepository.getOrderStatsBySeller(seller);
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
    public List<NFTSellOrder> getUserSellOrders(String userAddress) {
        return sellOrderRepository.findBySeller(userAddress);
    }
    
    /**
     * 특정 판매 주문 조회
     */
    public Optional<NFTSellOrder> getSellOrder(String orderId) {
        return sellOrderRepository.findByOrderId(orderId);
    }
    
    /**
     * 판매 주문 상태 업데이트
     */
    public NFTSellOrder updateSellOrderStatus(String orderId, UpdateOrderStatusRequest request) {
        Optional<NFTSellOrder> optionalOrder = sellOrderRepository.findByOrderId(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("판매 주문을 찾을 수 없습니다: " + orderId);
        }
        
        NFTSellOrder order = optionalOrder.get();
        order.setStatus(NFTSellOrder.OrderStatus.valueOf(request.getStatus().toUpperCase()));
        
        if (request.getBuyer() != null) {
            order.setBuyer(request.getBuyer());
        }
        
        if (request.getMatchedAt() != null) {
            order.setMatchedAt(request.getMatchedAt());
        }
        
        return sellOrderRepository.save(order);
    }
    
    /**
     * 판매 주문 취소
     */
    public void cancelSellOrder(String orderId, String userAddress) {
        Optional<NFTSellOrder> optionalOrder = sellOrderRepository.findByOrderId(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("판매 주문을 찾을 수 없습니다: " + orderId);
        }
        
        NFTSellOrder order = optionalOrder.get();
        if (!order.getSeller().equals(userAddress)) {
            throw new RuntimeException("주문을 취소할 권한이 없습니다");
        }
        
        order.setStatus(NFTSellOrder.OrderStatus.CANCELLED);
        sellOrderRepository.save(order);
    }
    
    
    // ==================== 구매 처리 관련 메서드 ====================
    
    /**
     * 구매 완료 확인
     */
    public void confirmPurchase(String orderId, ConfirmPurchaseRequest request) {
        Optional<NFTSellOrder> optionalOrder = sellOrderRepository.findByOrderId(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("판매 주문을 찾을 수 없습니다: " + orderId);
        }
        
        NFTSellOrder order = optionalOrder.get();
        if (order.getStatus() != NFTSellOrder.OrderStatus.LOCKED) {
            throw new RuntimeException("주문이 락 상태가 아닙니다");
        }
        
        if (!order.getLockedBy().equals(request.getBuyerAddress())) {
            throw new RuntimeException("잘못된 구매자입니다");
        }
        
        // 주문 완료 처리
        order.setStatus(NFTSellOrder.OrderStatus.COMPLETED);
        order.setBuyer(request.getBuyerAddress());
        order.setMatchedAt(System.currentTimeMillis());
        order.setLockedBy(null);
        order.setLockedAt(null);
        
        sellOrderRepository.save(order);
    }
    
    /**
     * 트랜잭션 실패 알림
     */
    public void reportTransactionFailure(String orderId, TransactionFailureRequest request) {
        Optional<NFTSellOrder> optionalOrder = sellOrderRepository.findByOrderId(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("판매 주문을 찾을 수 없습니다: " + orderId);
        }
        
        NFTSellOrder order = optionalOrder.get();
        if (order.getStatus() != NFTSellOrder.OrderStatus.LOCKED) {
            throw new RuntimeException("주문이 락 상태가 아닙니다");
        }
        
        if (!order.getLockedBy().equals(request.getBuyerAddress())) {
            throw new RuntimeException("잘못된 구매자입니다");
        }
        
        // 락 해제하고 활성 상태로 복구
        order.setStatus(NFTSellOrder.OrderStatus.ACTIVE);
        order.setLockedBy(null);
        order.setLockedAt(null);
        
        sellOrderRepository.save(order);
    }
    
    /**
     * 오프체인 서명 데이터 조회
     */
    public OffchainSignatureDataResponse getOffchainSignatureData(String orderId) {
        Optional<NFTSellOrder> optionalOrder = sellOrderRepository.findByOrderId(orderId);
        if (optionalOrder.isEmpty()) {
            throw new RuntimeException("판매 주문을 찾을 수 없습니다: " + orderId);
        }
        
        NFTSellOrder order = optionalOrder.get();
        
        // EIP-712 도메인 정보 (실제 환경에서는 설정에서 가져와야 함)
        OffchainSignatureDataResponse.Domain domain = new OffchainSignatureDataResponse.Domain(
            "NFTMarketplace",
            "1",
            1, // 체인 ID
            "0x..." // 컨트랙트 주소
        );
        
        // 타입 정의 (실제로는 EIP-712 표준에 맞게 구성)
        Map<String, Object> types = new HashMap<>();
        types.put("EIP712Domain", Arrays.asList(
            Map.of("name", "string", "type", "name"),
            Map.of("name", "string", "type", "version"),
            Map.of("name", "uint256", "type", "chainId"),
            Map.of("name", "address", "type", "verifyingContract")
        ));
        
        return new OffchainSignatureDataResponse(
            order,
            order.getSignature(),
            "0x...", // 메시지 해시 (실제로는 계산해야 함)
            domain,
            types
        );
    }
    
    // ==================== 통계 및 분석 메서드 ====================
    
    /**
     * 마켓 통계 조회
     */
    public MarketStatsResponse getMarketStats() {
        long totalSellOrders = sellOrderRepository.countByStatus(NFTSellOrder.OrderStatus.ACTIVE);
        
        String totalVolume = sellOrderRepository.getTotalVolume();
        String averagePrice = sellOrderRepository.getAveragePrice();
        
        return new MarketStatsResponse(
            (int) totalSellOrders,
            0, // 구매 주문 수는 0으로 설정
            totalVolume != null ? totalVolume : "0",
            averagePrice != null ? averagePrice : "0"
        );
    }
    
    /**
     * 인기 NFT 조회
     */
    public List<NFTSellOrder> getPopularNFTs(int limit) {
        return sellOrderRepository.findPopularActiveNFTs()
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    // ==================== 검색 및 필터링 메서드 ====================
    
    /**
     * NFT 검색
     */
    public List<NFTSellOrder> searchNFTs(String query, Map<String, String> filters) {
        List<NFTSellOrder> results = sellOrderRepository.searchActiveOrdersByQuery(query);
        
        // 필터 적용
        if (filters != null) {
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
            
            if (filters.containsKey("currency")) {
                results = results.stream()
                        .filter(order -> order.getCurrency().equalsIgnoreCase(filters.get("currency")))
                        .collect(Collectors.toList());
            }
        }
        
        return results;
    }
    
    /**
     * 가격 범위별 NFT 조회
     */
    public List<NFTSellOrder> getNFTsByPriceRange(String minPrice, String maxPrice) {
        return sellOrderRepository.findByPriceRange(
            NFTSellOrder.OrderStatus.ACTIVE,
            minPrice,
            maxPrice
        );
    }
}
