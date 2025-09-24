package com.example.toremainserver.controller;

import com.example.toremainserver.dto.nft.NftMintClientRequest;
import com.example.toremainserver.dto.nft.NftMintClientResponse;
import com.example.toremainserver.dto.nft.NftBurnClientRequest;
import com.example.toremainserver.dto.nft.NftBurnClientResponse;
import com.example.toremainserver.dto.nft.NftTransferClientRequest;
import com.example.toremainserver.dto.nft.NftTransferClientResponse;
import com.example.toremainserver.dto.nft.NftListClientRequest;
import com.example.toremainserver.dto.nft.NftListClientResponse;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.service.NftService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NftController {
    
    private final NftService nftService;
    
    @Autowired
    public NftController(NftService nftService) {
        this.nftService = nftService;
    }
    
    /**
     * UE5에서 아이템을 NFT화하는 요청을 받아 블록체인 서버로 전달
     * 
     * @param request NFT화 요청 (userId, itemId, userEquipItemId)
     * @return NFT화 결과 (성공여부, NFT ID 또는 오류 메시지)
     */
    @PostMapping("/nft/mint")
    public ResponseEntity<NftMintClientResponse> mintNft(@Valid @RequestBody NftMintClientRequest request) {
        try {
            NftMintClientResponse response = nftService.mintNft(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftMintClientResponse errorResponse = new NftMintClientResponse(false, "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * UE5에서 NFT화된 아이템을 burn(삭제)하는 요청을 받아 블록체인 서버로 전달
     * 
     * @param request NFT burn 요청 (userId, itemId, userEquipItemId, nftId)
     * @return NFT burn 결과 (성공여부 또는 오류 메시지)
     */
    @PostMapping("/nft/burn")
    public ResponseEntity<NftBurnClientResponse> burnNft(@Valid @RequestBody NftBurnClientRequest request) {
        try {
            NftBurnClientResponse response = nftService.burnNft(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftBurnClientResponse errorResponse = new NftBurnClientResponse(false, "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * UE5에서 NFT화된 아이템을 다른 사용자에게 전송하는 요청을 받아 블록체인 서버로 전달
     * 
     * @param request NFT transfer 요청 (userId, itemId, userEquipItemId, nftId, toUserId)
     * @return NFT transfer 결과 (성공여부 또는 오류 메시지)
     */
    @PostMapping("/nft/transfer")
    public ResponseEntity<NftTransferClientResponse> transferNft(@Valid @RequestBody NftTransferClientRequest request) {
        try {
            NftTransferClientResponse response = nftService.transferNft(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftTransferClientResponse errorResponse = new NftTransferClientResponse(false, "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * UE5에서 사용자의 지갑에 있는 모든 NFT화된 아이템 목록을 조회하고 소유권을 동기화하는 요청을 받아 블록체인 서버로 전달
     * 
     * @param userId 사용자 ID
     * @return 동기화된 NFT 목록 조회 결과 (성공여부, 아이템 데이터 목록 또는 오류 메시지)
     */
    @GetMapping("/nft/list/{userId}")
    public ResponseEntity<NftListClientResponse> getNftList(@PathVariable Long userId) {
        try {
            NftListClientRequest request = new NftListClientRequest(userId);
            NftListClientResponse response = nftService.getNftList(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftListClientResponse errorResponse = new NftListClientResponse(false, "서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * 특정 지갑 주소의 NFT화된 아이템 목록 조회
     * GET /api/nfts/user/{address}
     * 
     * @param address 지갑 주소
     * @return NFT화된 아이템 목록
     */
    @GetMapping("/nfts/user/{address}")
    public ResponseEntity<Map<String, Object>> getUserNftItems(@PathVariable String address) {
        try {
            List<UserEquipItem> nftItems = nftService.getUserNftItems(address);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", nftItems);
            response.put("count", nftItems.size());
            response.put("message", "사용자의 NFT화된 아이템 목록을 성공적으로 조회했습니다");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "서버 오류: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
