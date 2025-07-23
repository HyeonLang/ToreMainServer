package com.example.toremainserver.repository;

import com.example.toremainserver.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자(User) 엔티티에 대한 데이터베이스 접근을 담당하는 Repository 인터페이스
 * 
 * Repository의 역할:
 * 1. 데이터베이스와의 CRUD(Create, Read, Update, Delete) 작업을 추상화
 * 2. Spring Data JPA를 통해 자동으로 구현체를 생성
 * 3. 비즈니스 로직과 데이터 접근 로직을 분리
 * 4. 데이터베이스 쿼리 최적화 및 캐싱 기능 제공
 * 
 * 주요 기능:
 * - 기본 CRUD 작업: save(), findById(), findAll(), delete() 등
 * - 사용자명으로 사용자 검색
 * - 사용자명 중복 확인
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    /**
     * 사용자명으로 사용자를 검색하는 메서드
     * 
     * @param username 검색할 사용자명
     * @return Optional<User> - 사용자가 존재하면 User 객체를 포함, 없으면 빈 Optional
     * 
     * 사용 예시:
     * - 로그인 시 사용자 인증
     * - 사용자 정보 조회
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 사용자명이 이미 존재하는지 확인하는 메서드
     * 
     * @param username 확인할 사용자명
     * @return boolean - 사용자명이 존재하면 true, 없으면 false
     * 
     * 사용 예시:
     * - 회원가입 시 중복 사용자명 체크
     * - 사용자명 유효성 검증
     */
    boolean existsByUsername(String username);
} 