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
    
    // 사용자와 아이템 정의 ID로 조회
    List<UserEquipItem> findByUserIdAndItemDefId(Long userId, Long itemDefId);
    
    // NFT ID로 조회 (단일)
    Optional<UserEquipItem> findByNftId(String nftId);
    
    // NFT ID로 조회 (리스트) - 메서드명 변경
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.nftId = :nftId")
    List<UserEquipItem> findAllByNftId(@Param("nftId") String nftId);
    
    // 사용자별 특정 아이템 정의 개수 조회
    Long countByUserIdAndItemDefId(Long userId, Long itemDefId);
    
    // 지갑 주소로 NFT화된 아이템 조회 (nftId가 null이 아닌 것들)
    @Query("SELECT uei FROM UserEquipItem uei " +
           "JOIN User u ON uei.userId = u.id " +
           "WHERE u.walletAddress = :walletAddress AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByWalletAddress(@Param("walletAddress") String walletAddress);
    
    // 사용자 ID로 NFT화된 아이템 조회
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.userId = :userId AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByUserId(@Param("userId") Long userId);
    
    // 참고: 기본 제공 메서드
    // - Optional<UserEquipItem> findById(Long id)         // PK로 단일 장비 조회
    // - List<UserEquipItem> findByUserId(Long userId)     // 유저의 모든 장비
    // - UserEquipItem save(UserEquipItem item)            // 저장/수정
    // - void deleteById(Long id)                          // 삭제
} 