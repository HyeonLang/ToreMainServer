package com.example.toremainserver.dto.game;

public class ProfileCreateRequest {
    private Long userId;        // 프로필 소유자
    private String profileName; // 프로필 이름 (예: "메인 캐릭터", "서브1")

    public ProfileCreateRequest() {
    }

    public ProfileCreateRequest(Long userId, String profileName) {
        this.userId = userId;
        this.profileName = profileName;
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
}

