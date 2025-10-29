package com.example.toremainserver.dto.game;

import java.time.LocalDateTime;

public class GoldUpdateResponse {
    private Long profileId;      // userId 대신 profileId
    private Integer gold;        // 변경 후 Gold
    private Integer delta;       // 증감량
    private LocalDateTime updatedAt;    // 업데이트 시간

    public GoldUpdateResponse() {
    }

    public GoldUpdateResponse(Long profileId, Integer gold, Integer delta, LocalDateTime updatedAt) {
        this.profileId = profileId;
        this.gold = gold;
        this.delta = delta;
        this.updatedAt = updatedAt;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
        this.gold = gold;
    }

    public Integer getDelta() {
        return delta;
    }

    public void setDelta(Integer delta) {
        this.delta = delta;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

