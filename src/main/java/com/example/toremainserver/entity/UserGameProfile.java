package com.example.toremainserver.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "user_game_profiles",
       indexes = {
           @Index(name = "idx_user_id", columnList = "user_id")
       })
public class UserGameProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")  // DB 컬럼명은 명확하게
    private Long id;               // Java 필드명은 간결하게
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Integer level = 1;
    
    @Column(nullable = false)
    private Long experience = 0L;
    
    @Column(nullable = false)
    private Integer gold = 0;
    
    // 착용 중인 장비들 (슬롯명 -> localItemId 매핑)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "equipped_items", columnDefinition = "JSON")
    private Map<String, Long> equippedItems = new HashMap<>();
    
    // 스킬 정보 관리 (스킬명 -> 레벨 매핑)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "skill_info", columnDefinition = "JSON")
    private Map<String, Integer> skillInfo = new HashMap<>();
    
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;
    
    // 낙관적 락을 위한 버전 필드 (동시성 제어)
    @Version
    @Column(name = "version")
    private Long version;
    
    // 프로필 이름 (예: "메인 캐릭터", "서브1")
    @Column(name = "profile_name", nullable = false)
    private String profileName;
    
    // 연관 관계 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
    
    // 기본 생성자
    public UserGameProfile() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 생성자
    public UserGameProfile(Long userId, String profileName) {
        this.userId = userId;
        this.profileName = profileName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getter and Setter
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getProfileName() {
        return profileName;
    }
    
    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level;
    }
    
    public Long getExperience() {
        return experience;
    }
    
    public void setExperience(Long experience) {
        this.experience = experience;
    }
    
    public Integer getGold() {
        return gold;
    }
    
    public void setGold(Integer gold) {
        this.gold = gold;
    }
    
    public Map<String, Long> getEquippedItems() {
        return equippedItems;
    }
    
    public void setEquippedItems(Map<String, Long> equippedItems) {
        this.equippedItems = equippedItems;
    }
    
    public Map<String, Integer> getSkillInfo() {
        return skillInfo;
    }
    
    public void setSkillInfo(Map<String, Integer> skillInfo) {
        this.skillInfo = skillInfo;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public Long getVersion() {
        return version;
    }
    
    public void setVersion(Long version) {
        this.version = version;
    }
    
    // 장비 관련 유틸리티 메서드
    public void equipItem(String slotName, Long itemId) {
        this.equippedItems.put(slotName, itemId);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void unequipItem(String slotName) {
        this.equippedItems.remove(slotName);
        this.updatedAt = LocalDateTime.now();
    }
    
    public Long getEquippedItem(String slotName) {
        return this.equippedItems.get(slotName);
    }
    
    // 스킬 관련 유틸리티 메서드
    public void setSkillLevel(String skillName, Integer level) {
        this.skillInfo.put(skillName, level);
        this.updatedAt = LocalDateTime.now();
    }
    
    public Integer getSkillLevel(String skillName) {
        return this.skillInfo.getOrDefault(skillName, 0);
    }
    
    public void upgradeSkill(String skillName) {
        Integer currentLevel = getSkillLevel(skillName);
        setSkillLevel(skillName, currentLevel + 1);
    }
    
    // JPA 라이프사이클 콜백 - 엔티티 저장 전 자동 호출
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // JPA 라이프사이클 콜백 - 엔티티 업데이트 전 자동 호출
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

