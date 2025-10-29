package com.example.toremainserver.controller;

import com.example.toremainserver.dto.item.EquipItemRequest;
import com.example.toremainserver.dto.item.ConsumableItemRequest;
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
    public ResponseEntity<ItemDefinition> getItemDefinition(@PathVariable Long id) {
        ItemDefinition item = itemService.getItemDefinition(id);
        if (item != null) {
            return ResponseEntity.ok(item);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 사용자에게 소비 아이템 추가
    @PostMapping({"/consumable-item"})
    public ResponseEntity<?> addConsumableItemToUser(@RequestBody ConsumableItemRequest request) {
        try {
            UserConsumableItem userItem = itemService.addConsumableItemToUser(
                    request.getUserId(),
                    request.getItemDefId(),
                    request.getQuantity());
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 사용자에게 장비 아이템 추가
    @PostMapping("/equip-item")
    public ResponseEntity<?> addEquipItemToUser(@RequestBody EquipItemRequest request) {
        try {
            UserEquipItem userItem = itemService.addEquipItemToUser(
                request.getUserId(), 
                request.getItemDefId(), 
                request.getEnhancementData()
            );
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            // 에러 메시지를 응답으로 반환
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 사용자 소비 아이템 제거 (수량 감소 또는 삭제)
    @DeleteMapping({"/consumable-item", "/consumable-items"})
    public ResponseEntity<?> removeConsumableItemFromUser(
            @RequestParam Long userId,
            @RequestParam Long itemDefId,
            @RequestParam Integer quantity) {
        try {
            itemService.removeConsumableItemFromUser(userId, itemDefId, quantity);
            return ResponseEntity.ok(java.util.Map.of("message", "소비 아이템이 성공적으로 제거되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 사용자 장비 아이템 제거
    @DeleteMapping({"/equip-item", "/equip-items"})
    public ResponseEntity<?> removeEquipItemFromUser(@RequestParam Long equipItemId) {
        try {
            itemService.removeEquipItemFromUser(equipItemId);
            return ResponseEntity.ok(java.util.Map.of("message", "장비 아이템이 성공적으로 제거되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
