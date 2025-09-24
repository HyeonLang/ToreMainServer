package com.example.toremainserver.controller;

import com.example.toremainserver.dto.market.*;
import com.example.toremainserver.entity.NFTSellOrder;
import com.example.toremainserver.service.MarketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class MarketController {
    
    @Autowired
    private MarketService marketService;
    
    // ==================== 판매 주문 관련 API ====================
    
    /**
     * 판매 주문 생성
     * POST /sell-orders
     */
    @PostMapping("/sell-orders")
    public ResponseEntity<Map<String, Object>> createSellOrder(@Valid @RequestBody CreateSellOrderRequest request) {
        try {
            NFTSellOrder sellOrder = marketService.createSellOrder(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", sellOrder);
            response.put("message", "판매 주문이 성공적으로 생성되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 활성 판매 주문 목록 조회
     * GET /sell-orders?status=active
     */
    @GetMapping("/sell-orders")
    public ResponseEntity<Map<String, Object>> getActiveSellOrders(@RequestParam(defaultValue = "active") String status) {
        try {
            List<NFTSellOrder> orders;
            if ("active".equals(status)) {
                orders = marketService.getActiveSellOrders();
            } else {
                orders = marketService.getActiveSellOrders(); // 기본값
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 사용자의 판매 주문 조회
     * GET /sell-orders/user/:address
     */
    @GetMapping("/sell-orders/user/{address}")
    public ResponseEntity<Map<String, Object>> getUserSellOrders(@PathVariable String address) {
        try {
            List<NFTSellOrder> orders = marketService.getUserSellOrders(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 사용자의 활성 판매 주문 조회 (ACTIVE + LOCKED)
     * GET /sell-orders/user/:address/active
     */
    @GetMapping("/sell-orders/user/{address}/active")
    public ResponseEntity<Map<String, Object>> getUserActiveSellOrders(@PathVariable String address) {
        try {
            List<NFTSellOrder> orders = marketService.getActiveOrdersBySeller(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            response.put("message", "사용자 활성 판매 주문 목록을 성공적으로 조회했습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 사용자의 완료된 판매 주문 조회 (COMPLETED + CANCELLED)
     * GET /sell-orders/user/:address/completed
     */
    @GetMapping("/sell-orders/user/{address}/completed")
    public ResponseEntity<Map<String, Object>> getUserCompletedSellOrders(@PathVariable String address) {
        try {
            List<NFTSellOrder> orders = marketService.getCompletedOrdersBySeller(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            response.put("message", "사용자 완료된 판매 주문 목록을 성공적으로 조회했습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 사용자의 주문 통계 조회
     * GET /sell-orders/user/:address/stats
     */
    @GetMapping("/sell-orders/user/{address}/stats")
    public ResponseEntity<Map<String, Object>> getUserOrderStats(@PathVariable String address) {
        try {
            Map<String, Long> stats = marketService.getOrderStatsBySeller(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            response.put("message", "사용자 주문 통계를 성공적으로 조회했습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 판매 주문 조회
     * GET /sell-orders/:orderId
     */
    @GetMapping("/sell-orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getSellOrder(@PathVariable String orderId) {
        try {
            Optional<NFTSellOrder> order = marketService.getSellOrder(orderId);
            
            Map<String, Object> response = new HashMap<>();
            if (order.isPresent()) {
                response.put("success", true);
                response.put("data", order.get());
            } else {
                response.put("success", false);
                response.put("error", "판매 주문을 찾을 수 없습니다");
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 판매 주문 상태 업데이트
     * PUT /sell-orders/:orderId/status
     */
    @PutMapping("/sell-orders/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateSellOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        try {
            NFTSellOrder order = marketService.updateSellOrderStatus(orderId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", order);
            response.put("message", "주문 상태가 성공적으로 업데이트되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 판매 주문 취소
     * DELETE /sell-orders/:orderId
     */
    @DeleteMapping("/sell-orders/{orderId}")
    public ResponseEntity<Map<String, Object>> cancelSellOrder(
            @PathVariable String orderId,
            @Valid @RequestBody CancelOrderRequest request) {
        try {
            marketService.cancelSellOrder(orderId, request.getUserAddress());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "판매 주문이 성공적으로 취소되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    // ==================== 구매 처리 관련 API ====================
    
    /**
     * 구매 완료 확인
     * POST /sell-orders/:orderId/confirm
     */
    @PostMapping("/sell-orders/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmPurchase(
            @PathVariable String orderId,
            @Valid @RequestBody ConfirmPurchaseRequest request) {
        try {
            marketService.confirmPurchase(orderId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "구매가 성공적으로 완료되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 트랜잭션 실패 알림
     * POST /sell-orders/:orderId/failure
     */
    @PostMapping("/sell-orders/{orderId}/failure")
    public ResponseEntity<Map<String, Object>> reportTransactionFailure(
            @PathVariable String orderId,
            @Valid @RequestBody TransactionFailureRequest request) {
        try {
            marketService.reportTransactionFailure(orderId, request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "트랜잭션 실패가 보고되었습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 오프체인 서명 데이터 조회
     * GET /sell-orders/:orderId/offchain-data
     */
    @GetMapping("/sell-orders/{orderId}/offchain-data")
    public ResponseEntity<Map<String, Object>> getOffchainSignatureData(@PathVariable String orderId) {
        try {
            OffchainSignatureDataResponse data = marketService.getOffchainSignatureData(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ==================== 통계 및 분석 API ====================
    
    /**
     * 마켓 통계 조회
     * GET /market/stats
     */
    @GetMapping("/market/stats")
    public ResponseEntity<Map<String, Object>> getMarketStats() {
        try {
            MarketStatsResponse stats = marketService.getMarketStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 인기 NFT 조회
     * GET /market/popular?limit=:limit
     */
    @GetMapping("/market/popular")
    public ResponseEntity<Map<String, Object>> getPopularNFTs(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<NFTSellOrder> popularNFTs = marketService.getPopularNFTs(limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", popularNFTs);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ==================== 검색 및 필터링 API ====================
    
    /**
     * NFT 검색
     * GET /market/search?q=:query&minPrice=:min&maxPrice=:max&currency=:currency&category=:category
     */
    @GetMapping("/market/search")
    public ResponseEntity<Map<String, Object>> searchNFTs(
            @RequestParam String q,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) String category) {
        try {
            Map<String, String> filters = new HashMap<>();
            if (minPrice != null) filters.put("minPrice", minPrice);
            if (maxPrice != null) filters.put("maxPrice", maxPrice);
            if (currency != null) filters.put("currency", currency);
            if (category != null) filters.put("category", category);
            
            List<NFTSellOrder> results = marketService.searchNFTs(q, filters);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 가격 범위별 NFT 조회
     * GET /market/price-range?min=:min&max=:max
     */
    @GetMapping("/market/price-range")
    public ResponseEntity<Map<String, Object>> getNFTsByPriceRange(
            @RequestParam String min,
            @RequestParam String max) {
        try {
            List<NFTSellOrder> results = marketService.getNFTsByPriceRange(min, max);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", results);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
}
