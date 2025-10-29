package com.example.toremainserver.dto.game;

public class ExperienceUpdateRequest {
    private Long profileId;  // userId 대신 profileId
    private Long amount;     // 획득할 경험치 (양수만)

    public ExperienceUpdateRequest() {
    }

    public ExperienceUpdateRequest(Long profileId, Long amount) {
        this.profileId = profileId;
        this.amount = amount;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}

