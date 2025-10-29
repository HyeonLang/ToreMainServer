package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Npc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NpcRepository extends JpaRepository<Npc, Long> {
    
    // name으로 NPC 조회
    Optional<Npc> findByName(String name);
    
    // 참고: 기본 제공 메서드
    // - Optional<Npc> findById(Long id)      // PK로 NPC 조회
    // - boolean existsById(Long id)          // NPC 존재 여부 확인
    // - Npc save(Npc npc)                    // 저장/수정
    // - void deleteById(Long id)             // 삭제
} 