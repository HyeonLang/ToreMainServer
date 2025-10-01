package com.example.toremainserver.controller;

import com.example.toremainserver.dto.item.ItemRequest;
import com.example.toremainserver.dto.item.EquipItemRequest;
import com.example.toremainserver.entity.ItemDefinition;
import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserEquipItem;
import com.example.toremainserver.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ItemController {
    
    private final ItemService itemService;
    
    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }
    
    // 아이템 정의 추가
    @PostMapping("/item-definition")
    public ResponseEntity<ItemDefinition> createItemDefinition(@RequestBody ItemRequest itemRequest) {
        try {
            ItemDefinition createdItem = itemService.createItemDefinition(itemRequest);
            return ResponseEntity.ok(createdItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 모든 아이템 정의 조회
    @GetMapping("/item-definitions")
    public ResponseEntity<List<ItemDefinition>> getAllItemDefinitions() {
        List<ItemDefinition> items = itemService.getAllItemDefinitions();
        return ResponseEntity.ok(items);
    }
    
    // 사용자별 소비 아이템 조회
    @GetMapping("/consumable-items/{userId}")
    public ResponseEntity<List<UserConsumableItem>> getConsumableItemsByUserId(@PathVariable Long userId) {
        List<UserConsumableItem> items = itemService.getConsumableItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }
    
    // 사용자별 장비 아이템 조회
    @GetMapping("/equip-items/{userId}")
    public ResponseEntity<List<UserEquipItem>> getEquipItemsByUserId(@PathVariable Long userId) {
        List<UserEquipItem> items = itemService.getEquipItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }
    
    // 특정 아이템 정의 조회
    @GetMapping("/item-definition/{id}")
    public ResponseEntity<ItemDefinition> getItemDefinition(@PathVariable Integer id) {
        ItemDefinition item = itemService.getItemDefinition(id);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 아이템 정의 수정
    @PatchMapping("/item-definition/{id}")
    public ResponseEntity<ItemDefinition> updateItemDefinition(@PathVariable Integer id, @RequestBody ItemRequest itemRequest) {
        ItemDefinition updatedItem = itemService.updateItemDefinition(id, itemRequest);
        if (updatedItem != null) {
            return ResponseEntity.ok(updatedItem);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 아이템 정의 삭제
    @DeleteMapping("/item-definition/{id}")
    public ResponseEntity<Void> deleteItemDefinition(@PathVariable Integer id) {
        boolean deleted = itemService.deleteItemDefinition(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 사용자에게 소비 아이템 추가
    @PostMapping("/consumable-item")
    public ResponseEntity<UserConsumableItem> addConsumableItemToUser(
            @RequestParam Long userId, 
            @RequestParam Integer itemId, 
            @RequestParam Integer quantity,
            @RequestParam(required = false) Long localItemId) {
        try {
            UserConsumableItem userItem = itemService.addConsumableItemToUser(userId, itemId, quantity, localItemId);
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 사용자에게 장비 아이템 추가
    @PostMapping("/equip-item")
    public ResponseEntity<UserEquipItem> addEquipItemToUser(@RequestBody EquipItemRequest request) {
        try {
            UserEquipItem userItem = itemService.addEquipItemToUser(
                request.getUserId(), 
                request.getItemId(), 
                request.getLocalItemId(),
                request.getEnhancementData()
            );
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 