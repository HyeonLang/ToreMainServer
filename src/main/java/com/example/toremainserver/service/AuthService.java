package com.example.toremainserver.service;

import com.example.toremainserver.dto.LoginRequest;
import com.example.toremainserver.dto.LoginResponse;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    
    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(loginRequest.getPassword())) {
                return new LoginResponse(true, "로그인 성공", "jwt-token-" + user.getId() + "-" + System.currentTimeMillis());
            }
        }
        
        return new LoginResponse(false, "사용자명 또는 비밀번호가 잘못되었습니다", null);
    }
    
    // 사용자 등록 (테스트용)
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }
        
        User user = new User(username, password);
        return userRepository.save(user);
    }
} 