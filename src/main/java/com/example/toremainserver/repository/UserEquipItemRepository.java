package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserEquipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEquipItemRepository extends JpaRepository<UserEquipItem, Long> {
    
    // 사용자별 장비 아이템 조회
    List<UserEquipItem> findByUserId(Long userId);
    
    // 사용자와 아이템 ID로 조회
    List<UserEquipItem> findByUserIdAndItemId(Long userId, Integer itemId);
    
    // NFT ID로 조회 (단일)
    Optional<UserEquipItem> findByNftId(Long nftId);
    
    // NFT ID로 조회 (리스트) - 메서드명 변경
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.nftId = :nftId")
    List<UserEquipItem> findAllByNftId(@Param("nftId") Long nftId);
    
    // 사용자별 특정 아이템 개수 조회
    Long countByUserIdAndItemId(Long userId, Integer itemId);
    
    // 지갑 주소로 NFT화된 아이템 조회 (nftId가 null이 아닌 것들)
    @Query("SELECT uei FROM UserEquipItem uei " +
           "JOIN User u ON uei.userId = u.id " +
           "WHERE u.walletAddress = :walletAddress AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByWalletAddress(@Param("walletAddress") String walletAddress);
    
    // 사용자 ID로 NFT화된 아이템 조회
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.userId = :userId AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByUserId(@Param("userId") Long userId);
    
    // 사용자 ID, 아이템 ID, 로컬 아이템 ID로 조회
    Optional<UserEquipItem> findByUserIdAndItemIdAndLocalItemId(Long userId, Integer itemId, Long localItemId);
    
    // 사용자 ID와 로컬 아이템 ID로 조회
    Optional<UserEquipItem> findByUserIdAndLocalItemId(Long userId, Long localItemId);
} 