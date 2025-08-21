package com.example.toremainserver.service;

import com.example.toremainserver.dto.LoginRequest;
import com.example.toremainserver.dto.LoginResponse;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.auth.security.JwtTokenProvider;
import com.example.toremainserver.auth.dto.JwtAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    public AuthService(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    /**
     * 사용자 로그인 처리 및 JWT 토큰 생성
     * 
     * @param loginRequest 로그인 요청 정보 (사용자명, 비밀번호)
     * @return 로그인 응답 (성공/실패, 메시지, JWT 토큰 정보)
     * 
     * 처리 과정:
     * 1. 사용자명으로 사용자 정보 조회
     * 2. 비밀번호 검증
     * 3. 로그인 성공 시 JWT Access Token과 Refresh Token 생성
     * 4. 토큰 정보를 구조화된 형태로 반환
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
     * 
     * 클라이언트 사용법:
     * - Access Token: API 요청 시 Authorization 헤더에 "Bearer {accessToken}" 형태로 사용
     * - Refresh Token: 토큰 갱신 시 사용 (안전한 곳에 저장)
     * - expiresIn: Access Token 만료 시간 (초)
     */
    public LoginResponse login(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(loginRequest.getPassword())) {
                // 로그인 성공 시 JWT 토큰 생성
                String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
                String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
                
                // 구조화된 토큰 정보 반환 (클라이언트에서 파싱 불필요)
                return new LoginResponse(
                    true, 
                    "로그인 성공", 
                    accessToken, 
                    refreshToken, 
                    3600  // Access Token 만료 시간 (초)
                );
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
    
    /**
     * JWT 토큰 갱신
     * 
     * @param refreshToken 기존 Refresh Token
     * @return 새로운 Access Token과 Refresh Token
     * 
     * 처리 과정:
     * 1. Refresh Token 유효성 검증 (서명, 만료 여부)
     * 2. 토큰에서 사용자명 추출
     * 3. 데이터베이스에서 사용자 존재 확인
     * 4. 새로운 Access Token과 Refresh Token 생성
     * 5. 새로운 토큰 정보 반환
     * 
     * 사용 시나리오:
     * - Access Token 만료 시 클라이언트가 호출
     * - 보안을 위해 Refresh Token도 함께 갱신
     * 
     * @throws RuntimeException Refresh Token이 유효하지 않은 경우
     * @throws UsernameNotFoundException 사용자가 존재하지 않는 경우
     */
    public JwtAuthResponse refreshToken(String refreshToken) {
        // 1단계: Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        // 2단계: 토큰에서 사용자명 추출
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // 3단계: 데이터베이스에서 사용자 존재 확인
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
        
        // 4단계: 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        // 5단계: 새로운 토큰 정보 반환
        return new JwtAuthResponse(
            newAccessToken, 
            newRefreshToken, 
            3600, // access token 만료 시간 (초)
            username
        );
    }
    
    /**
     * JWT 로그아웃 (토큰 무효화)
     * 
     * @param refreshToken 무효화할 Refresh Token
     * 
     * 처리 과정:
     * 1. Refresh Token 검증 (선택적)
     * 2. 토큰을 블랙리스트에 추가하거나 무효화 처리
     * 3. 클라이언트에서 토큰 삭제 요청
     * 
     * 현재 구현:
     * - 단순히 로그만 출력 (개발용)
     * 
     * 실제 운영 환경에서는:
     * - Redis나 데이터베이스에 블랙리스트 저장
     * - JwtAuthenticationFilter에서 블랙리스트 확인
     * - 토큰 만료 시간까지 블랙리스트 유지
     * 
     * 보안 고려사항:
     * - Access Token은 짧은 유효기간으로 자동 만료
     * - Refresh Token 무효화로 세션 종료
     * - 클라이언트에서도 토큰 삭제 필요
     */
    public void logout(String refreshToken) {
        // 1단계: 토큰 검증 (선택적)
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            System.out.println("User logged out: " + username);
        }
        
        // 2단계: 토큰 무효화 처리
        // 실제 구현에서는 블랙리스트에 토큰 추가하거나
        // Redis 등을 사용하여 토큰을 무효화
        // 현재는 단순히 로그만 출력 (개발용)
        System.out.println("Token invalidated: " + refreshToken);
        
        // TODO: Redis 블랙리스트 구현
        // redisTemplate.opsForValue().set("blacklist:" + refreshToken, "true", Duration.ofSeconds(refreshTokenValidity));
    }
} 