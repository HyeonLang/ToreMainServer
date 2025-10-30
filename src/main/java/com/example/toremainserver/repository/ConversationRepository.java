package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // 특정 프로필 ID의 모든 대화 조회
    List<Conversation> findByProfileId(Long profileId);
    
    // 특정 NPC ID의 모든 대화 조회
    List<Conversation> findByNpcId(Long npcId);
    
    // 특정 프로필 ID와 NPC ID 간의 대화 조회
    Optional<Conversation> findByProfileIdAndNpcId(Long profileId, Long npcId);
    
    // 특정 프로필과 NPC 간의 대화 존재 여부 확인
    boolean existsByProfileIdAndNpcId(Long profileId, Long npcId);
}

