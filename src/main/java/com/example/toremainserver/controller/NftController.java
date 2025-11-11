package com.example.toremainserver.controller;

import com.example.toremainserver.dto.nft.NftMintClientRequest;
import com.example.toremainserver.dto.nft.NftMintClientResponse;
import com.example.toremainserver.dto.nft.NftListClientRequest;
import com.example.toremainserver.dto.nft.NftListClientResponse;
import com.example.toremainserver.dto.nft.NftLockUpRequest;
import com.example.toremainserver.dto.nft.NftLockUpResponse;
import com.example.toremainserver.dto.nft.NftUnlockUpRequest;
import com.example.toremainserver.dto.nft.NftUnlockUpResponse;
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
            NftMintClientResponse errorResponse = NftMintClientResponse.failure("서버 오류: " + e.getMessage());
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
     * NFT를 lockUp (계정 창고로 이동)
     * POST /api/nft/lockup
     * 
     * @param request lockUp 요청 (userId, equipItemId)
     * @return lockUp 결과
     */
    @PostMapping("/nft/lockup")
    public ResponseEntity<NftLockUpResponse> lockUpNft(@Valid @RequestBody NftLockUpRequest request) {
        try {
            NftLockUpResponse response = nftService.lockUpNft(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftLockUpResponse errorResponse = NftLockUpResponse.failure("서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * NFT를 unlockUp (블록체인으로 이동)
     * POST /api/nft/unlockup
     * 
     * @param request unlockUp 요청 (userId, nftId)
     * @return unlockUp 결과
     */
    @PostMapping("/nft/unlockup")
    public ResponseEntity<NftUnlockUpResponse> unlockUpNft(@Valid @RequestBody NftUnlockUpRequest request) {
        try {
            NftUnlockUpResponse response = nftService.unlockUpNft(request);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            NftUnlockUpResponse errorResponse = NftUnlockUpResponse.failure("서버 오류: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
}
