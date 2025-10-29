package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserConsumableItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConsumableItemRepository extends JpaRepository<UserConsumableItem, UserConsumableItemId> {
    
    // 사용자별 소비 아이템 조회
    List<UserConsumableItem> findByUserId(Long userId);
    
    // 사용자와 아이템 정의 ID로 조회
    Optional<UserConsumableItem> findByUserIdAndItemDefId(Long userId, Long itemDefId);
    
    // 사용자별 특정 아이템 정의 수량 조회
    Integer findQuantityByUserIdAndItemDefId(Long userId, Long itemDefId);
} 