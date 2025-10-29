package com.example.toremainserver.repository;

import com.example.toremainserver.entity.ItemDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemDefinitionRepository extends JpaRepository<ItemDefinition, Long> {
    
    // 아이템 타입별 조회
    List<ItemDefinition> findByType(ItemDefinition.ItemType type);
    
    // 이름으로 조회
    Optional<ItemDefinition> findByName(String name);
    
    // 이름으로 존재 여부 확인
    boolean existsByName(String name);
} 