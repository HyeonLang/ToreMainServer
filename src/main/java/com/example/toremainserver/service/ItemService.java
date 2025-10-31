package com.example.toremainserver.service;

import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.repository.ItemDefinitionRepository;
import com.example.toremainserver.repository.UserConsumableItemRepository;
import com.example.toremainserver.repository.UserEquipItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
public class ItemService {
    
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final UserConsumableItemRepository userConsumableItemRepository;
    private final UserEquipItemRepository userEquipItemRepository;
    
    @Autowired
    public ItemService(ItemDefinitionRepository itemDefinitionRepository, 
                      UserConsumableItemRepository userConsumableItemRepository,
                      UserEquipItemRepository userEquipItemRepository) {
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.userConsumableItemRepository = userConsumableItemRepository;
        this.userEquipItemRepository = userEquipItemRepository;
    }
    
    // 프로필별 소비 아이템 조회
    public List<UserConsumableItem> getConsumableItemsByProfileId(Long profileId) {
        return userConsumableItemRepository.findByProfileId(profileId);
    }
    
    // 프로필별 장비 아이템 조회
    public List<UserEquipItem> getEquipItemsByProfileId(Long profileId) {
        return userEquipItemRepository.findByProfileId(profileId);
    }
    
    // 아이템 정의 조회
    public ItemDefinition getItemDefinition(Long id) {
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(id);
        return itemOptional.orElse(null);
    }
    
    // 모든 아이템 정의 조회
    public List<ItemDefinition> getAllItemDefinitions() {
        return itemDefinitionRepository.findAll();
    }
    
    // 프로필에 소비 아이템 추가
    public UserConsumableItem addConsumableItemToProfile(Long profileId, Long itemDefId, Integer quantity) {
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(itemDefId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("아이템 정의를 찾을 수 없습니다.");
        }
        
        // profileId와 itemDefId로 기존 아이템 조회 (복합키)
        Optional<UserConsumableItem> existingItemOptional = userConsumableItemRepository.findByProfileIdAndItemDefId(profileId, itemDefId);
        
        if (existingItemOptional.isPresent()) {
            // 기존 아이템이 있으면 수량만 증가
            UserConsumableItem existingItem = existingItemOptional.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            return userConsumableItemRepository.save(existingItem);
        } else {
            // 없으면 새로 추가
            UserConsumableItem userItem = new UserConsumableItem(profileId, itemDefId, quantity);
            return userConsumableItemRepository.save(userItem);
        }
    }
    
    // 프로필에 장비 아이템 추가
    public UserEquipItem addEquipItemToProfile(Long profileId, Long itemDefId, Map<String, Object> enhancementData) {
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(itemDefId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("아이템 정의를 찾을 수 없습니다.");
        }
        
        // 단일 PK(id) 자동 생성
        UserEquipItem userItem = new UserEquipItem(profileId, itemDefId, enhancementData, null);
        return userEquipItemRepository.save(userItem);
    }
    
    // 프로필 소비 아이템 제거 (수량 감소 또는 삭제)
    public void removeConsumableItemFromProfile(Long profileId, Long itemDefId, Integer quantity) {
        // profileId와 itemDefId로 아이템 조회 (복합키)
        Optional<UserConsumableItem> userItemOptional = userConsumableItemRepository.findByProfileIdAndItemDefId(profileId, itemDefId);
        if (userItemOptional.isEmpty()) {
            throw new RuntimeException("프로필이 해당 소비 아이템을 보유하고 있지 않습니다.");
        }
        
        UserConsumableItem userItem = userItemOptional.get();
        
        if (quantity <= 0) {
            throw new RuntimeException("삭제할 수량은 0보다 커야 합니다.");
        }
        
        if (userItem.getQuantity() < quantity) {
            throw new RuntimeException("보유 수량(" + userItem.getQuantity() + ")보다 많은 수량을 삭제할 수 없습니다.");
        }
        
        // 삭제하려는 수량이 보유 수량보다 작으면 수량만 감소
        if (userItem.getQuantity() > quantity) {
            userItem.setQuantity(userItem.getQuantity() - quantity);
            userConsumableItemRepository.save(userItem);
        } else {
            // 수량이 같으면 아이템 자체를 삭제
            userConsumableItemRepository.delete(userItem);
        }
    }
    
    // 프로필 장비 아이템 제거
    public void removeEquipItemFromProfile(Long profileId, Long equipItemId) {

        // 단일 PK(id)로 직접 조회
        Optional<UserEquipItem> userItemOptional = userEquipItemRepository.findById(equipItemId);
        if (userItemOptional.isEmpty()) {
            throw new RuntimeException("해당 장비 아이템을 찾을 수 없습니다.");
        }
        
        UserEquipItem userItem = userItemOptional.get();

        if (userItem.getProfileId() != profileId) {
            throw new RuntimeException("해당 프로필에 소유권이 없습니다.");
        }
        
        // NFT화된 아이템은 삭제 불가
        if (userItem.getNftId() != null) {
            throw new RuntimeException("NFT화된 아이템은 삭제할 수 없습니다.");
        }
        
        userEquipItemRepository.delete(userItem);
    }
} 