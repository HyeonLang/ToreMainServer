package com.example.toremainserver.controller;

import com.example.toremainserver.dto.market.MarketStatsResponse;
import com.example.toremainserver.entity.NFTSellOrder;
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
