package com.example.toremainserver.dto.game;

import java.util.Map;

public class UserGameProfileUpdateRequest {
    private Long profileId;     // userId → profileId 변경
    private Integer level;
    private Long experience;
    private Integer gold;
    private Map<String, Long> equippedItems;
    private Map<String, Integer> skillInfo;

    public UserGameProfileUpdateRequest() {
    }

    public UserGameProfileUpdateRequest(Long profileId, Integer level, Long experience, Integer gold,
                                       Map<String, Long> equippedItems, Map<String, Integer> skillInfo) {
        this.profileId = profileId;
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

