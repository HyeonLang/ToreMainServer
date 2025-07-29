package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserEquipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEquipItemRepository extends JpaRepository<UserEquipItem, Long> {
    
    // 사용자별 장비 아이템 조회
    List<UserEquipItem> findByUserId(Long userId);
    
    // 사용자와 아이템 ID로 조회
    List<UserEquipItem> findByUserIdAndItemId(Long userId, Integer itemId);
    
    // NFT ID로 조회
    Optional<UserEquipItem> findByNftId(String nftId);
    
    // 사용자별 특정 아이템 개수 조회
    Long countByUserIdAndItemId(Long userId, Integer itemId);
} 