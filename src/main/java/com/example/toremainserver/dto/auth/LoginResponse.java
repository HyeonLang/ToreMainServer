package com.example.toremainserver.dto.auth;

/**
 * 로그인 응답 DTO
 * 
 * 클라이언트에게 로그인 결과와 JWT 토큰 정보를 전달합니다.
 * 
 * 응답 구조:
 * {
 *   "success": true,
 *   "message": "로그인 성공",
 *   "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 3600
 * }
 */
public class LoginResponse {
    private boolean success;
    private String message;
    private String accessToken;      // JWT Access Token
    private String refreshToken;     // JWT Refresh Token
    private String tokenType;        // 토큰 타입 (기본값: "Bearer")
    private long expiresIn;          // Access Token 만료 시간 (초)

    // 기본 생성자
    public LoginResponse() {
        this.tokenType = "Bearer";  // 기본값 설정
    }

    // 기존 호환성을 위한 생성자 (레거시)
    public LoginResponse(boolean success, String message, String token) {
        this();
        this.success = success;
        this.message = message;
        // 기존 방식: "accessToken|refreshToken" 형태의 문자열을 파싱
        if (token != null && token.contains("|")) {
            String[] tokens = token.split("\\|");
            this.accessToken = tokens[0];
            this.refreshToken = tokens[1];
        } else {
            this.accessToken = token;
        }
    }

    // 새로운 생성자 (JWT 토큰 분리)
    public LoginResponse(boolean success, String message, String accessToken, String refreshToken, long expiresIn) {
        this();
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    // Getter와 Setter
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // 레거시 호환성을 위한 getter (기존 코드에서 사용)
    public String getToken() {
        if (accessToken != null && refreshToken != null) {
            return accessToken + "|" + refreshToken;
        }
        return accessToken;
    }

    // 레거시 호환성을 위한 setter (기존 코드에서 사용)
    public void setToken(String token) {
        if (token != null && token.contains("|")) {
            String[] tokens = token.split("\\|");
            this.accessToken = tokens[0];
            this.refreshToken = tokens[1];
        } else {
            this.accessToken = token;
        }
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
