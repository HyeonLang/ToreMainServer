package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserGameProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserGameProfileRepository extends JpaRepository<UserGameProfile, Long> {
    
    // userId로 모든 프로필 조회
    List<UserGameProfile> findByUserId(Long userId);
    
    // userId와 profileName으로 조회
    Optional<UserGameProfile> findByUserIdAndProfileName(Long userId, String profileName);
    
    // userId로 프로필 개수 조회
    int countByUserId(Long userId);
    
    // 존재 여부 확인
    boolean existsByUserId(Long userId);
    
    // 참고: 기본 제공 메서드
    // - Optional<UserGameProfile> findById(Long profileId)  // PK로 조회
    // - UserGameProfile save(UserGameProfile profile)        // 저장/수정
    // - void deleteById(Long profileId)                      // 삭제
}

