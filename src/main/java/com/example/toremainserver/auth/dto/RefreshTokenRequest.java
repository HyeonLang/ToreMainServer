package com.example.toremainserver.auth.dto;

public class RefreshTokenRequest {
    private String refreshToken;
    
    // 기본 생성자
    public RefreshTokenRequest() {}
    
    // 생성자
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    // Getter와 Setter
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
