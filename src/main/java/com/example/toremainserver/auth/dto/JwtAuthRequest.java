package com.example.toremainserver.auth.dto;

public class JwtAuthRequest {
    private String username;
    private String password;
    
    // 기본 생성자
    public JwtAuthRequest() {}
    
    // 생성자
    public JwtAuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
    
    // Getter와 Setter
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
