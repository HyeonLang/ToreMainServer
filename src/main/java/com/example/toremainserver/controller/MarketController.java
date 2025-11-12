package com.example.toremainserver.controller;

import com.example.toremainserver.dto.market.MarketStatsResponse;
import com.example.toremainserver.entity.NFTMarketOrder;
import com.example.toremainserver.service.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MarketController {
    
    @Autowired
    private MarketService marketService;
    
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
    
    // ==================== 검색 및 필터링 API ====================
    
    /**
     * NFT 검색
     * GET /market/search?q=:query&minPrice=:min&maxPrice=:max&category=:category&status=:status
     * q: 아이템 이름 검색 (선택적, 없거나 빈 문자열이면 모든 아이템에 필터만 적용)
     */
    @GetMapping("/market/search")
    public ResponseEntity<Map<String, Object>> searchNFTs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status) {
        try {
            Map<String, String> filters = new HashMap<>();
            if (minPrice != null) filters.put("minPrice", minPrice);
            if (maxPrice != null) filters.put("maxPrice", maxPrice);
            if (category != null) filters.put("category", category);
            if (status != null) filters.put("status", status);
            
            // q가 null이거나 빈 문자열이면 빈 문자열로 전달
            String query = (q == null || q.trim().isEmpty()) ? "" : q.trim();
            List<NFTMarketOrder> results = marketService.searchNFTs(query, filters);
            
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
    
    // ==================== 판매 주문 조회 API ====================
    
    /**
     * 사용자 지갑 주소로 판매 주문 조회
     * GET /market/sell-orders/user/:userAddress
     * 
     * @param userAddress 판매자 지갑 주소
     * @return 해당 판매자의 모든 NFTMarketOrder 리스트
     */
    @GetMapping("/market/search/user/{userAddress}")
    public ResponseEntity<Map<String, Object>> getUserSellOrders(
            @PathVariable String userAddress) {
        try {
            List<NFTMarketOrder> orders = marketService.getUserSellOrders(userAddress);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ==================== 이벤트 리스너 API ====================
    
    /**
     * MarketplaceVault 이벤트 리스너용 DB 갱신 API
     * POST /api/market/vault-event
     * 
     * @param request 이벤트 요청 (tokenId, buyerWalletAddress)
     * @return DB 갱신 결과
     */
    @PostMapping("/market/vault-event")
    public ResponseEntity<Map<String, Object>> handleMarketplaceVaultEvent(
            @RequestBody Map<String, String> request) {
        try {
            String tokenId = request.get("tokenId");
            String buyerWalletAddress = request.get("buyerWalletAddress");
            
            if (tokenId == null || tokenId.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("error", "tokenId는 필수입니다");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean result = marketService.updateMarketOrderByMarketplaceVaultEvent(tokenId, buyerWalletAddress);
            
            Map<String, Object> response = new HashMap<>();
            if (result) {
                response.put("success", true);
                response.put("message", "마켓플레이스 주문이 성공적으로 갱신되었습니다");
            } else {
                response.put("success", false);
                response.put("error", "주문을 찾을 수 없거나 갱신에 실패했습니다");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "서버 오류: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
