package com.example.toremainserver.controller;

import com.example.toremainserver.dto.item.EquipItemRequest;
import com.example.toremainserver.dto.item.ConsumableItemRequest;
import com.example.toremainserver.dto.item.UpdateLocationRequest;
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
    
    // 프로필별 소비 아이템 조회
    @GetMapping("/consumable-items/profile/{profileId}")
    public ResponseEntity<List<UserConsumableItem>> getConsumableItemsByProfileId(@PathVariable Long profileId) {
        List<UserConsumableItem> items = itemService.getConsumableItemsByProfileId(profileId);
        return ResponseEntity.ok(items);
    }
    
    // 프로필별 장비 아이템 조회
    @GetMapping("/equip-items/profile/{profileId}")
    public ResponseEntity<List<UserEquipItem>> getEquipItemsByProfileId(@PathVariable Long profileId) {
        List<UserEquipItem> items = itemService.getEquipItemsByProfileId(profileId);
        return ResponseEntity.ok(items);
    }
    
    // userId로 유저의 모든 소비 아이템 조회
    @GetMapping("/consumable-items/user/{userId}")
    public ResponseEntity<List<UserConsumableItem>> getConsumableItemsByUserId(@PathVariable Long userId) {
        List<UserConsumableItem> items = itemService.getConsumableItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }
    
    // userId로 유저의 모든 장비 아이템 조회
    @GetMapping("/equip-items/user/{userId}")
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
    
    // 프로필에 소비 아이템 추가
    @PostMapping({"/consumable-item"})
    public ResponseEntity<?> addConsumableItemToProfile(@RequestBody ConsumableItemRequest request) {
        try {
            UserConsumableItem userItem = itemService.addConsumableItemToProfile(
                    request.getProfileId(),
                    request.getItemDefId(),
                    request.getQuantity());
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 프로필에 장비 아이템 추가
    @PostMapping("/equip-item")
    public ResponseEntity<?> addEquipItemToProfile(@RequestBody EquipItemRequest request) {
        try {
            UserEquipItem userItem = itemService.addEquipItemToProfile(
                request.getProfileId(), 
                request.getItemDefId(), 
                request.getEnhancementData()
            );
            return ResponseEntity.ok(userItem);
        } catch (RuntimeException e) {
            // 에러 메시지를 응답으로 반환
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 프로필 소비 아이템 제거 (수량 감소 또는 삭제)
    @DeleteMapping({"/consumable-item", "/consumable-items"})
    public ResponseEntity<?> removeConsumableItemFromProfile(
            @RequestParam Long profileId,
            @RequestParam Long itemDefId,
            @RequestParam Integer quantity) {
        try {
            itemService.removeConsumableItemFromProfile(profileId, itemDefId, quantity);
            return ResponseEntity.ok(java.util.Map.of("message", "소비 아이템이 성공적으로 제거되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 프로필 장비 아이템 제거
    @DeleteMapping({"/equip-item", "/equip-items"})
    public ResponseEntity<?> removeEquipItemFromProfile(
        @RequestParam Long profileId,
        @RequestParam Long equipItemId
        ) {
        try {
            itemService.removeEquipItemFromProfile(profileId, equipItemId);
            return ResponseEntity.ok(java.util.Map.of("message", "장비 아이템이 성공적으로 제거되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
    
    // 장비 아이템의 locationId 업데이트 (PATCH)
    @PatchMapping("/equip-item/{equipItemId}/location")
    public ResponseEntity<?> updateEquipItemLocation(
        @PathVariable Long equipItemId,
        @RequestBody UpdateLocationRequest request
        ) {
        try {
            itemService.updateLocationId(equipItemId, request.getLocationId(), request.getProfileId());
            return ResponseEntity.ok(java.util.Map.of("message", "locationId가 성공적으로 업데이트되었습니다"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
