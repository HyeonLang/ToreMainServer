package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserEquipItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEquipItemRepository extends JpaRepository<UserEquipItem, Long> {
    
    // 프로필별 장비 아이템 조회
    List<UserEquipItem> findByProfileId(Long profileId);
    
    // userId로 장비 아이템 조회
    List<UserEquipItem> findByUserId(Long userId);
    
    // userId와 locationId로 장비 아이템 조회
    List<UserEquipItem> findByUserIdAndLocationId(Long userId, Integer locationId);
    
    // 여러 프로필 ID로 장비 아이템 조회
    List<UserEquipItem> findByProfileIdIn(List<Long> profileIds);
    
    // 프로필과 아이템 정의 ID로 조회
    List<UserEquipItem> findByProfileIdAndItemDefId(Long profileId, Long itemDefId);
    
    // NFT ID로 조회 (단일)
    Optional<UserEquipItem> findByNftId(String nftId);
    
    // NFT ID로 조회 (리스트) - 메서드명 변경
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.nftId = :nftId")
    List<UserEquipItem> findAllByNftId(@Param("nftId") String nftId);
    
    // 프로필별 특정 아이템 정의 개수 조회
    Long countByProfileIdAndItemDefId(Long profileId, Long itemDefId);
    
    // userId로 NFT화된 아이템 조회 (nftId가 null이 아닌 것들)
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.userId = :userId AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByUserId(@Param("userId") Long userId);
    
    // 지갑 주소로 NFT화된 아이템 조회 (nftId가 null이 아닌 것들)
    // userId를 통해 직접 조회 (더 효율적)
    @Query("SELECT uei FROM UserEquipItem uei " +
           "JOIN User u ON uei.userId = u.id " +
           "WHERE u.walletAddress = :walletAddress AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByWalletAddress(@Param("walletAddress") String walletAddress);
    
    // 프로필 ID로 NFT화된 아이템 조회
    @Query("SELECT uei FROM UserEquipItem uei WHERE uei.profileId = :profileId AND uei.nftId IS NOT NULL")
    List<UserEquipItem> findNftItemsByProfileId(@Param("profileId") Long profileId);
    
    // locationId만 업데이트 (전용 쿼리 - 성능 최적화)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEquipItem uei SET uei.locationId = :locationId WHERE uei.id = :id")
    int updateLocationId(@Param("id") Long id, @Param("locationId") Integer locationId);
    
    // locationId와 profileId 함께 업데이트 (전용 쿼리 - 성능 최적화)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE UserEquipItem uei SET uei.locationId = :locationId, uei.profileId = :profileId WHERE uei.id = :id")
    int updateLocationIdAndProfileId(@Param("id") Long id, @Param("locationId") Integer locationId, @Param("profileId") Long profileId);
    
    // locationId 업데이트하고 profileId를 null로 설정 (네이티브 쿼리 사용 - NULL 설정 보장)
    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE user_equip_items SET location_id = :locationId, profile_id = NULL WHERE id = :id", nativeQuery = true)
    int updateLocationIdAndSetProfileIdToNull(@Param("id") Long id, @Param("locationId") Integer locationId);
    
    // 참고: 기본 제공 메서드
    // - Optional<UserEquipItem> findById(Long id)         // PK로 단일 장비 조회
    // - List<UserEquipItem> findByProfileId(Long profileId)     // 프로필의 모든 장비
    // - UserEquipItem save(UserEquipItem item)            // 저장/수정
    // - void deleteById(Long id)                          // 삭제
} 