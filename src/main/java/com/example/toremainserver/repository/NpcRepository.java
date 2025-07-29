package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Npc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NpcRepository extends JpaRepository<Npc, Long> {
    
    // npcId로 NPC 조회
    Optional<Npc> findByNpcId(Long npcId);
    
    // name으로 NPC 조회
    Optional<Npc> findByName(String name);
    
    // npcId로 NPC 존재 여부 확인
    boolean existsByNpcId(Long npcId);
} 