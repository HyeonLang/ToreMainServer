package com.example.toremainserver.repository;

import com.example.toremainserver.entity.UserGameProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGameProfileRepository extends JpaRepository<UserGameProfile, Long> {
    
    // 존재 여부 확인
    boolean existsByUserId(Long userId);
    
    // 참고: 기본 제공 메서드
    // - Optional<UserGameProfile> findById(Long userId)  // 전체 조회
    // - UserGameProfile save(UserGameProfile profile)     // 저장/수정
    // - void deleteById(Long userId)                      // 삭제
}

