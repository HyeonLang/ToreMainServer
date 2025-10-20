package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    
    // 특정 유저 ID의 모든 대화 조회
    List<Conversation> findByUserId(Long userId);
    
    // 특정 NPC ID의 모든 대화 조회
    List<Conversation> findByNpcId(Long npcId);
    
    // 특정 유저 ID와 NPC ID 간의 대화 조회
    Optional<Conversation> findByUserIdAndNpcId(Long userId, Long npcId);
    
    // 특정 유저와 NPC 간의 대화 존재 여부 확인
    boolean existsByUserIdAndNpcId(Long userId, Long npcId);
}

