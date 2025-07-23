package com.example.toremainserver.repository;

import com.example.toremainserver.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 아이템(Item) 엔티티에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 * 
 * Repository의 역할:
 * 1. 데이터베이스와의 CRUD(Create, Read, Update, Delete) 작업을 추상화
 * 2. Spring Data JPA를 통해 자동으로 구현체를 생성
 * 3. 비즈니스 로직과 데이터 접근 로직을 분리
 * 4. 데이터베이스 쿼리 최적화 및 캐싱 기능 제공
 * 5. 복잡한 쿼리 로직을 메서드명으로 표현
 * 
 * 주요 기능:
 * - 기본 CRUD 작업: save(), findById(), findAll(), delete() 등
 * - 사용자별 아이템 조회
 * - 아이템 검색 및 필터링
 * 
 * Spring Data JPA가 제공하는 기본 메서드들:
 * - save(Item item): 아이템 저장/수정
 * - findById(String id): ID로 아이템 조회
 * - findAll(): 모든 아이템 조회
 * - deleteById(String id): ID로 아이템 삭제
 * - existsById(String id): ID로 아이템 존재 여부 확인
 * - count(): 전체 아이템 개수 조회
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, String> {
    
    /**
     * 특정 사용자가 소유한 모든 아이템을 조회하는 메서드
     * 
     * @param userId 조회할 사용자의 ID
     * @return List<Item> - 해당 사용자가 소유한 아이템 목록
     * 
     * 사용 예시:
     * - 사용자별 아이템 목록 조회
     * - 사용자의 인벤토리 확인
     * - 사용자별 아이템 통계
     * 
     * JPA 메서드 명명 규칙:
     * - findBy: 조회 메서드
     * - UserId: User 엔티티의 id 필드를 기준으로 검색
     * - 자동으로 "SELECT i FROM Item i WHERE i.user.id = ?1" 쿼리 생성
     */
    List<Item> findByUserId(String userId);
} 