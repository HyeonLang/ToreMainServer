package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserConsumableItem;
import com.example.toremainserver.entity.UserConsumableItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConsumableItemRepository extends JpaRepository<UserConsumableItem, UserConsumableItemId> {
    
    // 프로필별 소비 아이템 조회
    List<UserConsumableItem> findByProfileId(Long profileId);
    
    // 프로필과 아이템 정의 ID로 조회
    Optional<UserConsumableItem> findByProfileIdAndItemDefId(Long profileId, Long itemDefId);
    
    // 프로필별 특정 아이템 정의 수량 조회
    Integer findQuantityByProfileIdAndItemDefId(Long profileId, Long itemDefId);
} 