package com.example.toremainserver.dto.game;

import java.time.LocalDateTime;
import java.util.Map;

public class UserGameProfileResponse {
    private Long profileId;     // userId → profileId 변경
    private Long userId;        // 프로필 소유자 정보
    private String profileName; // 프로필 이름
    private Integer level;
    private Long experience;
    private Integer gold;
    private Map<String, Long> equippedItems;
    private Map<String, Integer> skillInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserGameProfileResponse() {
    }

    public UserGameProfileResponse(Long profileId, Long userId, String profileName, Integer level, Long experience, Integer gold,
                                  Map<String, Long> equippedItems, Map<String, Integer> skillInfo,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.profileId = profileId;
        this.userId = userId;
        this.profileName = profileName;
        this.level = level;
        this.experience = experience;
        this.gold = gold;
        this.equippedItems = equippedItems;
        this.skillInfo = skillInfo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
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
}

