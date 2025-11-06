package com.example.toremainserver.service;

import com.example.toremainserver.dto.nft.NftMintClientRequest;
import com.example.toremainserver.dto.nft.NftMintClientResponse;
import com.example.toremainserver.dto.nft.ContractNftRequest;
import com.example.toremainserver.dto.nft.ContractNftResponse;
import com.example.toremainserver.dto.nft.NftBurnClientRequest;
import com.example.toremainserver.dto.nft.NftBurnClientResponse;
import com.example.toremainserver.dto.nft.ContractNftBurnRequest;
import com.example.toremainserver.dto.nft.ContractNftBurnResponse;
import com.example.toremainserver.dto.nft.NftListClientRequest;
import com.example.toremainserver.dto.nft.NftListClientResponse;
import com.example.toremainserver.dto.nft.ContractNftListRequest;
import com.example.toremainserver.dto.nft.ContractNftListResponse;
import com.example.toremainserver.dto.item.ItemData;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.UserGameProfile;
import com.example.toremainserver.repository.ItemDefinitionRepository;
import com.example.toremainserver.repository.UserEquipItemRepository;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.repository.UserGameProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class NftService {
    private static final Logger logger = LoggerFactory.getLogger(NftService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemDefinitionRepository itemDefinitionRepository;
    
    @Autowired
    private UserEquipItemRepository userEquipItemRepository;
    
    @Autowired
    private UserGameProfileRepository userGameProfileRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${blockchain.server.url:http://localhost:3000}")
    private String blockchainServerUrl;
    
    @Value("${blockchain.contract.address:0x1234567890abcdef}")
    private String contractAddress;
    
    @Value("${server.url:http://localhost:8080}")
    private String serverUrl;
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            if (user.getWalletAddress() == null || user.getWalletAddress().isEmpty()) {
                return NftMintClientResponse.failure("사용자의 지갑 주소가 설정되지 않았습니다");
            }
            
            // 2. 프로필 조회 및 소유권 검증
            UserGameProfile profile = userGameProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new RuntimeException("프로필을 찾을 수 없습니다: " + request.getProfileId()));
            
            // userId와 profileId의 소유권 관계 검증
            if (!profile.getUserId().equals(request.getUserId())) {
                return NftMintClientResponse.failure("해당 프로필에 대한 권한이 없습니다");
            }
            
            // 3. UserEquipItem 조회 (단일 PK 사용)
            UserEquipItem userEquipItem = userEquipItemRepository.findById(request.getEquipItemId())
                .orElseThrow(() -> new RuntimeException("사용자 장비 아이템을 찾을 수 없습니다: " + request.getEquipItemId()));
            
            // 4. 아이템 소유권 검증
            if (!userEquipItem.getProfileId().equals(request.getProfileId())) {
                return NftMintClientResponse.failure("해당 아이템에 대한 권한이 없습니다");
            }

            // 5. 아이템 정의 조회
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(userEquipItem.getItemDefId())
                .orElseThrow(() -> new RuntimeException("아이템 정의를 찾을 수 없습니다: " + userEquipItem.getItemDefId()));

            // 6. 아이템이 이미 NFT화되었는지 확인
            if (userEquipItem.getNftId() != null) {
                return NftMintClientResponse.failure("이미 NFT화된 아이템입니다");
            }
            
            // 7. 아이템 데이터 구성
            Map<String, Object> itemData = createItemData(itemDefinition, userEquipItem);
            
            // 8. metadataUrl 생성
            String metadataUrl = serverUrl + "/api/metadata/" + userEquipItem.getId();
            
            // 9. 블록체인 서버로 요청 전송
            ContractNftRequest contractRequest = new ContractNftRequest(
                user.getWalletAddress(),
                userEquipItem.getItemDefId(),
                userEquipItem.getId(),
                itemData,
                metadataUrl
            );
            
            ContractNftResponse contractResponse = sendToBlockchainServer(contractRequest);
            
            if (contractResponse != null && contractResponse.isSuccess()) {
                // 10. 성공 시 UserEquipItem에 NFT ID 업데이트
                userEquipItem.setNftId(contractResponse.getNftId());
                userEquipItem.setLocationId(0);
                userEquipItem.setProfileId(null);
                userEquipItem.setUserId(request.getUserId());  // NFT 소유권 설정
                userEquipItemRepository.save(userEquipItem);
                
                return NftMintClientResponse.success(contractResponse.getNftId());
            } else {
                return NftMintClientResponse.failure("블록체인 서버 응답 오류");
            }
            
        } catch (Exception e) {
            return NftMintClientResponse.failure("NFT화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createItemData(ItemDefinition itemDefinition, UserEquipItem userEquipItem) {
        Map<String, Object> itemData = new HashMap<>();
        
        // NFT 메타데이터 표준 형식
        itemData.put("name", itemDefinition.getName());
        
        // 설명 추가
        
        itemData.put("description", itemDefinition.getDescription());
        
        
        // 아이템 타입 추가
        itemData.put("type", itemDefinition.getType().name());
        
        // 이미지 URL 추가
        String ipfsImageUrl = itemDefinition.getIpfsImageUrl();
        itemData.put("image", ipfsImageUrl);

        // base_stats를 gameData에 추가
        if (itemDefinition.getBaseStats() != null) {
            itemData.put("baseStats", itemDefinition.getBaseStats());
        }

        
        return itemData;
    }
    
    private ContractNftResponse sendToBlockchainServer(ContractNftRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ContractNftRequest> entity = new HttpEntity<>(request, headers);
            
            String url = blockchainServerUrl + "/api/blockchain/nft/mint";
            ResponseEntity<ContractNftResponse> response = restTemplate.postForEntity(
                url, entity, ContractNftResponse.class);
            
            // HTTP 200 성공 응답만 반환 (실패는 예외로 처리됨)
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return null;
            }
            
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            // HTTP 4xx/5xx 오류는 예외로 처리됨
            // 상태 코드 기반으로 실패 처리
            logger.error("블록체인 서버 오류 응답: HTTP {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("블록체인 서버 통신 오류", e);
            return null;
        }
    }
    
    public NftBurnClientResponse burnNft(NftBurnClientRequest request) {
        try {
            // 1. 블록체인 서버로 burn 요청 전송
            ContractNftBurnRequest contractRequest = new ContractNftBurnRequest(
                request.getUserAddress(),
                request.getTokenId(),
                request.getContractAddress()
            );
            
            ContractNftBurnResponse contractResponse = sendBurnToBlockchainServer(contractRequest);
            
            if (contractResponse.isSuccess()) {
                // 2. 성공 시 데이터베이스에서 해당 NFT ID를 가진 UserEquipItem 찾아서 NFT ID 제거
                List<UserEquipItem> nftItems = userEquipItemRepository.findAllByNftId(request.getTokenId());
                for (UserEquipItem item : nftItems) {
                    item.setNftId(null);
                    userEquipItemRepository.save(item);
                }
                
                return new NftBurnClientResponse(true);
            } else {
                return new NftBurnClientResponse(false, contractResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            return new NftBurnClientResponse(false, "NFT burn 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private ContractNftBurnResponse sendBurnToBlockchainServer(ContractNftBurnRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<ContractNftBurnRequest> entity = new HttpEntity<>(request, headers);
            
            String url = blockchainServerUrl + "/api/blockchain/nft/burn";
            ResponseEntity<ContractNftBurnResponse> response = restTemplate.postForEntity(
                url, entity, ContractNftBurnResponse.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            return new ContractNftBurnResponse(false, "블록체인 서버 통신 오류: " + e.getMessage());
        }
    }
    
    private ContractNftListResponse getNftListFromBlockchainServer(ContractNftListRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // GET 요청을 위한 URL 파라미터 구성
            String url = UriComponentsBuilder.fromUriString(blockchainServerUrl + "/api/blockchain/nft/list")
                .queryParam("walletAddress", request.getWalletAddress())
                .queryParam("contractAddress", request.getContractAddress())
                .toUriString();
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<ContractNftListResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, ContractNftListResponse.class);
            
            return response.getBody();
            
        } catch (Exception e) {
            return new ContractNftListResponse(false, "블록체인 서버 통신 오류: " + e.getMessage());
        }
    }
    
    
    /**
     * 블록체인에서 받은 NFT ID 목록과 DB의 사용자 소유권을 동기화
     * 
     * @param walletAddress 지갑 주소
     * @param nftIdList 블록체인에서 받은 NFT ID 목록
     */
    public void syncNftOwnership(String walletAddress, List<String> nftIdList) {
        // 1. 지갑 주소로 사용자 조회
        User user = userRepository.findByWalletAddress(walletAddress);
        if (user == null) {
            throw new RuntimeException("지갑 주소에 해당하는 사용자를 찾을 수 없습니다: " + walletAddress);
        }
        
        // 2. 각 NFT ID에 대해 소유권 확인 및 동기화
        for (String nftId : nftIdList) {
            Optional<UserEquipItem> userEquipItemOpt = userEquipItemRepository.findByNftId(nftId);
            
            if (userEquipItemOpt.isPresent()) {
                // 3. 소유권이 다르면 userId 동기화
                UserEquipItem userEquipItem = userEquipItemOpt.get();
                if (!user.getId().equals(userEquipItem.getUserId())) {
                    userEquipItem.setUserId(user.getId());
                    userEquipItemRepository.save(userEquipItem);
                }
            } else {
                // 4. NFT ID가 DB에 없으면 로그 기록 (새로 생성된 NFT일 수 있음)
                System.out.println("Warning: NFT ID " + nftId + " not found in database for wallet " + walletAddress);
            }
        }
    }
    
    /**
     * 사용자의 NFT 목록을 조회하고 소유권을 동기화
     * 
     * @param request NFT 목록 조회 요청
     * @return 동기화된 NFT 목록 응답
     */
    public NftListClientResponse getNftList(NftListClientRequest request) {
        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            if (user.getWalletAddress() == null || user.getWalletAddress().isEmpty()) {
                return new NftListClientResponse(false, "사용자의 지갑 주소가 설정되지 않았습니다");
            }
            
            // 2. 블록체인 서버로 NFT 목록 조회 요청
            ContractNftListRequest contractRequest = new ContractNftListRequest(
                user.getWalletAddress(),
                contractAddress
            );
            
            ContractNftListResponse contractResponse = getNftListFromBlockchainServer(contractRequest);
            
            if (contractResponse.isSuccess()) {
                // 3. 소유권 동기화
                syncNftOwnership(user.getWalletAddress(), contractResponse.getNftIdList());
                
                // 4. 아이템 데이터 조회
                List<ItemData> itemDataList = getItemDataByNftIds(contractResponse.getNftIdList());
                
                return new NftListClientResponse(true, itemDataList);
            } else {
                return new NftListClientResponse(false, contractResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            return new NftListClientResponse(false, "NFT 목록 조회 및 동기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * NFT ID 목록으로부터 아이템 데이터를 조회
     * 
     * @param nftIds NFT ID 목록
     * @return 아이템 데이터 목록
     */
    private List<ItemData> getItemDataByNftIds(List<String> nftIds) {
        List<ItemData> itemDataList = new ArrayList<>();
        
        for (String nftId : nftIds) {
            // NFT ID로 UserEquipItem 조회
            Optional<UserEquipItem> userEquipItemOpt = userEquipItemRepository.findByNftId(nftId);
            
            if (userEquipItemOpt.isPresent()) {
                UserEquipItem userEquipItem = userEquipItemOpt.get();
                ItemData itemData = new ItemData(
                    userEquipItem.getItemDefId(),
                    userEquipItem.getEnhancementData()
                );
                itemDataList.add(itemData);
            }
        }
        
        return itemDataList;
    }
    
    /**
     * 지갑 주소로 NFT화된 아이템 목록 조회 (메타데이터 형식)
     */
    public List<Map<String, Object>> getUserNftItems(String walletAddress) {
        List<UserEquipItem> nftItems = userEquipItemRepository.findNftItemsByWalletAddress(walletAddress);
        List<Map<String, Object>> metadataList = new ArrayList<>();
        
        for (UserEquipItem userEquipItem : nftItems) {
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(userEquipItem.getItemDefId())
                .orElseThrow(() -> new RuntimeException("ItemDefinition not found: " + userEquipItem.getItemDefId()));
            
            Map<String, Object> metadata = createItemData(itemDefinition, userEquipItem);
            metadataList.add(metadata);
        }
        
        return metadataList;
    }
    
    /**
     * 프로필 ID로 NFT화된 아이템 목록 조회
     */
    public List<UserEquipItem> getUserNftItemsByProfileId(Long profileId) {
        return userEquipItemRepository.findNftItemsByProfileId(profileId);
    }
}
