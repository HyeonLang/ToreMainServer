package com.example.toremainserver.dto.game;

import java.time.LocalDateTime;

public class ExperienceUpdateResponse {
    private Long profileId;         // userId 대신 profileId
    private Integer level;          // 현재 레벨
    private Long experience;        // 변경 후 경험치
    private Long delta;             // 증가량
    private LocalDateTime updatedAt;

    public ExperienceUpdateResponse() {
    }

    public ExperienceUpdateResponse(Long profileId, Integer level, Long experience, Long delta, 
                                   LocalDateTime updatedAt) {
        this.profileId = profileId;
        this.level = level;
        this.experience = experience;
        this.delta = delta;
        this.updatedAt = updatedAt;
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

    public Long getDelta() {
        return delta;
    }

    public void setDelta(Long delta) {
        this.delta = delta;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

