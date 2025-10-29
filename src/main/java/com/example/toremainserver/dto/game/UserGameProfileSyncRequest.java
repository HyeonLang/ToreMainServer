package com.example.toremainserver.dto.game;

import java.util.Map;

public class UserGameProfileSyncRequest {
    private Long profileId;     // userId → profileId 변경 (기존 프로필 업데이트용)
    private Long userId;        // 프로필 소유자 (신규 생성 시 필요)
    private String profileName; // 프로필 이름 (신규 생성 시 필요)
    private Integer level;
    private Long experience;
    private Integer gold;
    private Map<String, Long> equippedItems;
    private Map<String, Integer> skillInfo;

    public UserGameProfileSyncRequest() {
    }

    public UserGameProfileSyncRequest(Long profileId, Long userId, String profileName, Integer level, Long experience, Integer gold,
                                     Map<String, Long> equippedItems, Map<String, Integer> skillInfo) {
        this.profileId = profileId;
        this.userId = userId;
        this.profileName = profileName;
        this.level = level;
        this.experience = experience;
        this.gold = gold;
        this.equippedItems = equippedItems;
        this.skillInfo = skillInfo;
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
}

