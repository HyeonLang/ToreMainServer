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
import com.example.toremainserver.repository.ItemDefinitionRepository;
import com.example.toremainserver.repository.UserEquipItemRepository;
import com.example.toremainserver.repository.UserRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class NftService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemDefinitionRepository itemDefinitionRepository;
    
    @Autowired
    private UserEquipItemRepository userEquipItemRepository;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${blockchain.server.url:http://localhost:3000}")
    private String blockchainServerUrl;
    
    @Value("${blockchain.contract.address:0x1234567890abcdef}")
    private String contractAddress;
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        try {
            // 1. 사용자 정보 조회
            User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + request.getUserId()));
            
            if (user.getWalletAddress() == null || user.getWalletAddress().isEmpty()) {
                return NftMintClientResponse.failure("사용자의 지갑 주소가 설정되지 않았습니다");
            }
            
            // 2. 아이템 정의 조회
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(request.getItemId())
                .orElseThrow(() -> new RuntimeException("아이템 정의를 찾을 수 없습니다: " + request.getItemId()));

            // 3. UserEquipItem에서 사용자 id 아이템 id 로컬 id로 알맞는 장비 찾기
            UserEquipItem userEquipItem = userEquipItemRepository.findByUserIdAndItemIdAndLocalItemId(
                request.getUserId(), 
                request.getItemId(), 
                request.getLocalItemId()
            ).orElseThrow(() -> new RuntimeException("사용자 장비 아이템을 찾을 수 없습니다"));

            // 4. 아이템이 이미 NFT화되었는지 확인
            if (userEquipItem.getNftId() != null) {
                return NftMintClientResponse.failure("이미 NFT화된 아이템입니다");
            }
            
            // 5. 아이템 데이터 구성
            Map<String, Object> itemData = createItemData(itemDefinition, userEquipItem);
            
            // 6. 블록체인 서버로 요청 전송
            ContractNftRequest contractRequest = new ContractNftRequest(
                user.getWalletAddress(),
                request.getItemId(),
                userEquipItem.getId(),
                itemData
            );
            
            ContractNftResponse contractResponse = sendToBlockchainServer(contractRequest);
            
            if (contractResponse.isSuccess()) {
                // 7. 성공 시 UserEquipItem에 NFT ID 업데이트
                userEquipItem.setNftId(contractResponse.getNftId());
                userEquipItemRepository.save(userEquipItem);
                
                return NftMintClientResponse.success(contractResponse.getNftId());
            } else {
                return NftMintClientResponse.failure(contractResponse.getErrorMessage());
            }
            
        } catch (Exception e) {
            return NftMintClientResponse.failure("NFT화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private Map<String, Object> createItemData(ItemDefinition itemDefinition, UserEquipItem userEquipItem) {
        Map<String, Object> itemData = new HashMap<>();
        
        // NFT 메타데이터 표준 형식
        itemData.put("name", itemDefinition.getName());
        itemData.put("description", itemDefinition.getDescription());
        
        // IPFS 이미지 URL (ipfs:// 형식으로 변환)
        String ipfsImageUrl = itemDefinition.getIpfsImageUrl();
        if (ipfsImageUrl != null && !ipfsImageUrl.trim().isEmpty()) {
            // https://ipfs.io/ipfs/QmXXX/filename.png -> ipfs://QmXXX/filename.png
            if (ipfsImageUrl.contains("/ipfs/")) {
                String cidAndFile = ipfsImageUrl.substring(ipfsImageUrl.lastIndexOf("/ipfs/") + 6);
                itemData.put("image", "ipfs://" + cidAndFile);
            } else {
                itemData.put("image", ipfsImageUrl);
            }
        } else {
            // IPFS 이미지가 없으면 로컬 이미지 사용
            String localImageUrl = itemDefinition.getImageUrl();
            if (localImageUrl != null && !localImageUrl.trim().isEmpty()) {
                itemData.put("image", localImageUrl);
            }
        }
        
        // External URL (게임 웹사이트)
        itemData.put("external_url", "https://toregame.com/items/" + userEquipItem.getId());
        
        // Attributes 배열 생성
        List<Map<String, Object>> attributes = new ArrayList<>();
        
        // base_stats를 attributes로 변환
        if (itemDefinition.getBaseStats() != null) {
            for (Map.Entry<String, Object> entry : itemDefinition.getBaseStats().entrySet()) {
                Map<String, Object> attribute = new HashMap<>();
                attribute.put("trait_type", entry.getKey());
                attribute.put("value", entry.getValue());
                attributes.add(attribute);
            }
        }
        
        // enhancement_data를 attributes로 변환
        if (userEquipItem.getEnhancementData() != null) {
            for (Map.Entry<String, Object> entry : userEquipItem.getEnhancementData().entrySet()) {
                Map<String, Object> attribute = new HashMap<>();
                attribute.put("trait_type", entry.getKey());
                attribute.put("value", entry.getValue());
                attributes.add(attribute);
            }
        }
        
        itemData.put("attributes", attributes);
        
        // Game data
        Map<String, Object> gameData = new HashMap<>();
        gameData.put("id", "item_" + userEquipItem.getId());
        gameData.put("item_id", itemDefinition.getId().toString());
        gameData.put("nft_id", userEquipItem.getNftId());
        itemData.put("game_data", gameData);
        
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
            
            return response.getBody();
            
        } catch (Exception e) {
            return ContractNftResponse.failure("블록체인 서버 통신 오류: " + e.getMessage());
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
                UserEquipItem userEquipItem = userEquipItemOpt.get();
                
                // 3. 소유권이 다르면 동기화
                if (!userEquipItem.getUserId().equals(user.getId())) {
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
                    userEquipItem.getItemId(),
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
            ItemDefinition itemDefinition = itemDefinitionRepository.findById(userEquipItem.getItemId())
                .orElseThrow(() -> new RuntimeException("ItemDefinition not found: " + userEquipItem.getItemId()));
            
            Map<String, Object> metadata = createItemData(itemDefinition, userEquipItem);
            metadataList.add(metadata);
        }
        
        return metadataList;
    }
    
    /**
     * 사용자 ID로 NFT화된 아이템 목록 조회
     */
    public List<UserEquipItem> getUserNftItemsByUserId(Long userId) {
        return userEquipItemRepository.findNftItemsByUserId(userId);
    }
}
