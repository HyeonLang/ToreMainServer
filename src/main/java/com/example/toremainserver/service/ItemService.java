package com.example.toremainserver.service;

import com.example.toremainserver.dto.ItemRequest;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.ItemDefinitionRepository;
import com.example.toremainserver.repository.UserConsumableItemRepository;
import com.example.toremainserver.repository.UserEquipItemRepository;
import com.example.toremainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class ItemService {
    
    private final ItemDefinitionRepository itemDefinitionRepository;
    private final UserConsumableItemRepository userConsumableItemRepository;
    private final UserEquipItemRepository userEquipItemRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public ItemService(ItemDefinitionRepository itemDefinitionRepository, 
                      UserConsumableItemRepository userConsumableItemRepository,
                      UserEquipItemRepository userEquipItemRepository,
                      UserRepository userRepository) {
        this.itemDefinitionRepository = itemDefinitionRepository;
        this.userConsumableItemRepository = userConsumableItemRepository;
        this.userEquipItemRepository = userEquipItemRepository;
        this.userRepository = userRepository;
    }
    
    // 아이템 정의 추가
    public ItemDefinition createItemDefinition(ItemRequest itemRequest) {
        Map<String, Object> baseStats = new HashMap<>();
        baseStats.put("price", itemRequest.getPrice());
        
        ItemDefinition itemDefinition = new ItemDefinition(
            itemRequest.getName(), 
            ItemDefinition.ItemType.ETC, 
            baseStats, 
            itemRequest.getDescription()
        );
        
        return itemDefinitionRepository.save(itemDefinition);
    }
    
    // 사용자별 소비 아이템 조회
    public List<UserConsumableItem> getConsumableItemsByUserId(Long userId) {
        return userConsumableItemRepository.findByUserId(userId);
    }
    
    // 사용자별 장비 아이템 조회
    public List<UserEquipItem> getEquipItemsByUserId(Long userId) {
        return userEquipItemRepository.findByUserId(userId);
    }
    
    // 아이템 정의 조회
    public ItemDefinition getItemDefinition(Integer id) {
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(id);
        return itemOptional.orElse(null);
    }
    
    // 모든 아이템 정의 조회
    public List<ItemDefinition> getAllItemDefinitions() {
        return itemDefinitionRepository.findAll();
    }
    
    // 아이템 정의 수정
    public ItemDefinition updateItemDefinition(Integer id, ItemRequest itemRequest) {
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(id);
        if (itemOptional.isPresent()) {
            ItemDefinition existingItem = itemOptional.get();
            existingItem.setName(itemRequest.getName());
            existingItem.setDescription(itemRequest.getDescription());
            
            Map<String, Object> baseStats = new HashMap<>();
            baseStats.put("price", itemRequest.getPrice());
            existingItem.setBaseStats(baseStats);
            
            return itemDefinitionRepository.save(existingItem);
        }
        return null;
    }
    
    // 아이템 정의 삭제
    public boolean deleteItemDefinition(Integer id) {
        if (itemDefinitionRepository.existsById(id)) {
            itemDefinitionRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    // 사용자에게 소비 아이템 추가
    public UserConsumableItem addConsumableItemToUser(Long userId, Integer itemId, Integer quantity, Long localItemId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("아이템 정의를 찾을 수 없습니다.");
        }
        
        // localItemId가 null이면 자동 생성, 아니면 사용자가 제공한 값 사용
        if (localItemId == null) {
            localItemId = userId * 1000L + itemId;
        }
        
        UserConsumableItem userItem = new UserConsumableItem(userId, itemId, quantity, localItemId);
        return userConsumableItemRepository.save(userItem);
    }
    
    // 사용자에게 장비 아이템 추가
    public UserEquipItem addEquipItemToUser(Long userId, Integer itemId, Long localItemId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }
        
        Optional<ItemDefinition> itemOptional = itemDefinitionRepository.findById(itemId);
        if (itemOptional.isEmpty()) {
            throw new RuntimeException("아이템 정의를 찾을 수 없습니다.");
        }
        
        // localItemId가 null이면 자동 생성, 아니면 사용자가 제공한 값 사용
        if (localItemId == null) {
            localItemId = userId * 2000L + itemId;
        }
        
        UserEquipItem userItem = new UserEquipItem(userId, itemId, null, null, localItemId);
        return userEquipItemRepository.save(userItem);
    }
} 