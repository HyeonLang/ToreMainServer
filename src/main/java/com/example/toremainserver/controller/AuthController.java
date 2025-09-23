package com.example.toremainserver.controller;

import com.example.toremainserver.dto.auth.LoginRequest;
import com.example.toremainserver.dto.auth.LoginResponse;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.service.AuthService;
import com.example.toremainserver.auth.dto.RefreshTokenRequest;
import com.example.toremainserver.auth.dto.JwtAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    
    private final AuthService authService;
    
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody LoginRequest registerRequest) {
        try {
            User user = authService.registerUser(registerRequest.getUsername(), registerRequest.getPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "사용자 등록 성공");
            response.put("userId", user.getId());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * JWT 토큰 갱신
     */
    @PostMapping("/auth/refresh")
    public ResponseEntity<JwtAuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            JwtAuthResponse response = authService.refreshToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * JWT 로그아웃
     */
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 