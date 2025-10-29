package com.example.toremainserver.dto.game;

public class GoldUpdateRequest {
    private Long profileId;  // userId 대신 profileId 사용
    private Integer amount;  // 양수: 증가, 음수: 감소

    public GoldUpdateRequest() {
    }

    public GoldUpdateRequest(Long profileId, Integer amount) {
        this.profileId = profileId;
        this.amount = amount;
    }

    public Long getProfileId() {
        return profileId;
    }

    public void setProfileId(Long profileId) {
        this.profileId = profileId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}

