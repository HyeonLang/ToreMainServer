# 🔒 보안 개선 가이드

> 현업에서 사용하는 실전 보안 개선 방법

---

## 📋 목차

1. [비밀번호 보안](#1-비밀번호-보안)
2. [Spring Security 설정](#2-spring-security-설정)
3. [JWT 보안 강화](#3-jwt-보안-강화)
4. [환경 변수 관리](#4-환경-변수-관리)
5. [SQL Injection 방어](#5-sql-injection-방어)
6. [CORS 설정](#6-cors-설정)
7. [보안 헤더 설정](#7-보안-헤더-설정)
8. [API Rate Limiting](#8-api-rate-limiting)

---

## 1. 비밀번호 보안

### ⚠️ 현재 문제점
```java
// ❌ 절대 안됨! 평문 비밀번호 비교
if (user.getPassword().equals(loginRequest.getPassword())) {
    // 로그인 성공
}
```

**위험성**:
- DB 유출 시 모든 사용자 계정 탈취 가능
- 내부자의 비밀번호 열람 가능
- GDPR, 개인정보보호법 위반

### ✅ 해결 방법

#### 1.1 BCrypt 적용 (권장)

**의존성 추가** (이미 있음):
```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

**1) PasswordEncoder Bean 활성화**

```java
// SecurityConfig.java (주석 해제)
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // strength: 12 (권장: 10-14)
    }
}
```

**2) 회원가입 시 비밀번호 해싱**

```java
// AuthService.java
@Service
public class AuthService {
    
    private final PasswordEncoder passwordEncoder;
    
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("이미 존재하는 사용자명입니다.");
        }
        
        // ✅ 비밀번호 해싱
        String hashedPassword = passwordEncoder.encode(password);
        
        User user = new User(username, hashedPassword);
        return userRepository.save(user);
    }
}
```

**3) 로그인 시 비밀번호 검증**

```java
// AuthService.java
public LoginResponse login(LoginRequest loginRequest) {
    User user = userRepository.findByUsername(loginRequest.getUsername())
        .orElseThrow(() -> new InvalidCredentialsException("사용자명 또는 비밀번호가 잘못되었습니다"));
    
    // ✅ BCrypt로 비밀번호 검증
    if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
        throw new InvalidCredentialsException("사용자명 또는 비밀번호가 잘못되었습니다");
    }
    
    // JWT 토큰 생성
    String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
    String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
    
    return new LoginResponse(true, "로그인 성공", accessToken, refreshToken, 3600);
}
```

#### 1.2 기존 사용자 비밀번호 마이그레이션

**평문 → 해시 마이그레이션 스크립트**

```java
@Service
public class PasswordMigrationService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 평문 비밀번호를 해시로 변환
     * ⚠️ 주의: 실행 전 백업 필수!
     */
    @Transactional
    public void migratePasswords() {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String plainPassword = user.getPassword();
            
            // 이미 해시된 비밀번호는 건너뛰기 (BCrypt는 $2a$로 시작)
            if (plainPassword.startsWith("$2a$") || plainPassword.startsWith("$2b$")) {
                continue;
            }
            
            // 해시 적용
            String hashedPassword = passwordEncoder.encode(plainPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            log.info("Password migrated for user: {}", user.getUsername());
        }
    }
}
```

#### 1.3 비밀번호 정책 적용

```java
@Service
public class PasswordValidator {
    
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 100;
    
    /**
     * 비밀번호 강도 검증
     * - 최소 8자 이상
     * - 대문자, 소문자, 숫자, 특수문자 중 3가지 이상 포함
     */
    public void validate(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            throw new WeakPasswordException("비밀번호는 최소 8자 이상이어야 합니다");
        }
        
        if (password.length() > MAX_LENGTH) {
            throw new WeakPasswordException("비밀번호는 최대 100자까지 가능합니다");
        }
        
        int strength = 0;
        if (password.matches(".*[a-z].*")) strength++; // 소문자
        if (password.matches(".*[A-Z].*")) strength++; // 대문자
        if (password.matches(".*\\d.*")) strength++;   // 숫자
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) strength++; // 특수문자
        
        if (strength < 3) {
            throw new WeakPasswordException(
                "비밀번호는 대문자, 소문자, 숫자, 특수문자 중 3가지 이상을 포함해야 합니다"
            );
        }
    }
}
```

---

## 2. Spring Security 설정

### ⚠️ 현재 문제점
```properties
# application.properties
spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```
```java
// SecurityConfig.java - 주석 처리됨
//@Configuration
//@EnableWebSecurity
```

**위험성**:
- 모든 API가 인증 없이 접근 가능
- JWT 필터가 작동하지 않음
- CSRF, XSS 등 기본 보안 기능 비활성화

### ✅ 해결 방법

#### 2.1 Spring Security 활성화

**1) application.properties 수정**
```properties
# ❌ 제거
# spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
```

**2) SecurityConfig 활성화**

```java
package com.example.toremainserver.auth.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용 시)
            .csrf(csrf -> csrf.disable())
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 사용하지 않음 (Stateless)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 인증 예외 처리
            .exceptionHandling(exception -> 
                exception.authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 엔드포인트별 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 엔드포인트 (인증 불필요)
                .requestMatchers(
                    "/api/login",
                    "/api/register",
                    "/api/auth/refresh",
                    "/health",
                    "/actuator/**"
                ).permitAll()
                
                // Swagger 문서 (개발 환경에서만)
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                
                // NFT 관련 API (인증 필요)
                .requestMatchers("/api/nft/**").authenticated()
                
                // 게임 이벤트 API (인증 필요)
                .requestMatchers("/api/npc", "/api/material").authenticated()
                
                // 마켓 API (일부는 공개, 일부는 인증 필요)
                .requestMatchers(HttpMethod.GET, "/api/sell-orders/**").permitAll()
                .requestMatchers("/api/sell-orders/**").authenticated()
                
                // 관리자 전용 API
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 기타 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // JWT 필터 추가
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 (운영 환경에서는 구체적으로 명시)
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React 개발 서버
            "http://localhost:8080",  // 로컬 테스트
            "https://yourdomain.com"  // 운영 도메인
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With"
        ));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 프리플라이트 요청 캐싱 시간 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

#### 2.2 JWT 인증 진입점 구현

```java
package com.example.toremainserver.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 인증되지 않은 사용자의 접근 처리
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) 
            throws IOException, ServletException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "UNAUTHORIZED");
        errorResponse.put("message", "인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", request.getRequestURI());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
```

#### 2.3 메서드 레벨 보안

```java
@Service
public class NftService {
    
    /**
     * 관리자만 접근 가능
     */
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAllNfts() {
        // 위험한 작업
    }
    
    /**
     * 본인 또는 관리자만 접근 가능
     */
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public List<UserEquipItem> getUserNfts(Long userId) {
        return userEquipItemRepository.findNftItemsByUserId(userId);
    }
}
```

---

## 3. JWT 보안 강화

### ⚠️ 현재 문제점
```properties
# application.properties
jwt.secret=your-secret-key-here-make-it-long-and-secure-for-production
jwt.access-token-validity=3600
jwt.refresh-token-validity=86400
```

**위험성**:
- 약한 시크릿 키 (Git에 노출됨)
- 토큰 탈취 시 무효화 불가능
- Refresh Token 관리 부재

### ✅ 해결 방법

#### 3.1 강력한 Secret Key 생성

```java
// SecretKeyGenerator.java (한 번만 실행)
public class SecretKeyGenerator {
    public static void main(String[] args) {
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.println("JWT Secret Key: " + base64Key);
        // 출력 예: JWT Secret Key: 3cfa76ef14937c1c0ea519bd485219be7b99c7fb...
    }
}
```

#### 3.2 환경 변수로 Secret Key 관리

**application.properties**
```properties
# ❌ 제거
# jwt.secret=your-secret-key-here-make-it-long-and-secure-for-production

# ✅ 환경 변수에서 읽기
jwt.secret=${JWT_SECRET_KEY}
jwt.access-token-validity=${JWT_ACCESS_VALIDITY:3600}
jwt.refresh-token-validity=${JWT_REFRESH_VALIDITY:604800}
```

**환경 변수 설정 (운영 서버)**
```bash
# Linux/Mac
export JWT_SECRET_KEY="3cfa76ef14937c1c0ea519bd485219be7b99c7fb..."
export JWT_ACCESS_VALIDITY=3600
export JWT_REFRESH_VALIDITY=604800

# Windows
set JWT_SECRET_KEY=3cfa76ef14937c1c0ea519bd485219be7b99c7fb...
```

**Docker 환경**
```yaml
# docker-compose.yml
services:
  app:
    environment:
      - JWT_SECRET_KEY=${JWT_SECRET_KEY}
      - JWT_ACCESS_VALIDITY=3600
      - JWT_REFRESH_VALIDITY=604800
```

#### 3.3 Refresh Token 관리 (Redis)

**의존성 추가**
```gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
```

**Redis 설정**
```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    
    @Value("${spring.redis.host:localhost}")
    private String host;
    
    @Value("${spring.redis.port:6379}")
    private int port;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        return new LettuceConnectionFactory(config);
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**Refresh Token 저장 및 검증**
```java
@Service
public class RefreshTokenService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final long REFRESH_TOKEN_VALIDITY = 604800; // 7일
    
    /**
     * Refresh Token 저장
     */
    public void saveRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(
            key, 
            refreshToken, 
            Duration.ofSeconds(REFRESH_TOKEN_VALIDITY)
        );
    }
    
    /**
     * Refresh Token 검증
     */
    public boolean validateRefreshToken(String username, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + username;
        String storedToken = (String) redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken);
    }
    
    /**
     * Refresh Token 삭제 (로그아웃)
     */
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }
}
```

**AuthService 수정**
```java
@Service
public class AuthService {
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    public LoginResponse login(LoginRequest loginRequest) {
        // ... 비밀번호 검증 로직 ...
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        // ✅ Refresh Token Redis에 저장
        refreshTokenService.saveRefreshToken(user.getUsername(), refreshToken);
        
        return new LoginResponse(true, "로그인 성공", accessToken, refreshToken, 3600);
    }
    
    public JwtAuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("Invalid refresh token");
        }
        
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
        
        // ✅ Redis에서 Refresh Token 검증
        if (!refreshTokenService.validateRefreshToken(username, refreshToken)) {
            throw new InvalidTokenException("Refresh token not found or expired");
        }
        
        // 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);
        
        // ✅ 새로운 Refresh Token 저장
        refreshTokenService.saveRefreshToken(username, newRefreshToken);
        
        return new JwtAuthResponse(newAccessToken, newRefreshToken, 3600, username);
    }
    
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            
            // ✅ Refresh Token 삭제
            refreshTokenService.deleteRefreshToken(username);
        }
    }
}
```

#### 3.4 Access Token 블랙리스트 (로그아웃 처리)

```java
@Service
public class TokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "blacklist:";
    
    /**
     * Access Token을 블랙리스트에 추가
     */
    public void addToBlacklist(String token, long expirationTime) {
        String key = BLACKLIST_PREFIX + token;
        long ttl = expirationTime - System.currentTimeMillis() / 1000;
        
        if (ttl > 0) {
            redisTemplate.opsForValue().set(key, "true", Duration.ofSeconds(ttl));
        }
    }
    
    /**
     * 토큰이 블랙리스트에 있는지 확인
     */
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
```

**JwtAuthenticationFilter 수정**
```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null) {
            // ✅ 블랙리스트 확인
            if (tokenBlacklistService.isBlacklisted(token)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, 
                    "Token has been revoked");
                return;
            }
            
            // 토큰 검증 및 인증 처리...
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 4. 환경 변수 관리

### ⚠️ 현재 문제점
```properties
# application.properties - Git에 커밋됨!
spring.datasource.username=root
spring.datasource.password=1234
jwt.secret=your-secret-key-here
blockchain.server.url=http://localhost:3000
```

### ✅ 해결 방법

#### 4.1 .env 파일 사용

**.env 파일 생성** (Git에 커밋하지 않음!)
```bash
# .env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=tore
DB_USERNAME=root
DB_PASSWORD=your_secure_password

JWT_SECRET_KEY=your_generated_secret_key_here
JWT_ACCESS_VALIDITY=3600
JWT_REFRESH_VALIDITY=604800

BLOCKCHAIN_SERVER_URL=http://localhost:3000
BLOCKCHAIN_CONTRACT_ADDRESS=0x5FbDB2315678afecb367f032d93F642f64180aa3

AI_SERVER_URL=http://localhost:8000

REDIS_HOST=localhost
REDIS_PORT=6379
```

**.gitignore에 추가**
```gitignore
# Environment variables
.env
.env.local
.env.*.local

# Application properties with secrets
application-prod.properties
application-local.properties
```

**application.properties 수정**
```properties
# Database
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:tore}?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT
jwt.secret=${JWT_SECRET_KEY}
jwt.access-token-validity=${JWT_ACCESS_VALIDITY:3600}
jwt.refresh-token-validity=${JWT_REFRESH_VALIDITY:604800}

# Blockchain
blockchain.server.url=${BLOCKCHAIN_SERVER_URL}
blockchain.contract.address=${BLOCKCHAIN_CONTRACT_ADDRESS}

# AI Server
ai.server.url=${AI_SERVER_URL}

# Redis
spring.redis.host=${REDIS_HOST:localhost}
spring.redis.port=${REDIS_PORT:6379}
```

#### 4.2 프로파일별 설정 분리

**application-dev.properties** (개발 환경)
```properties
# 개발 환경 설정
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=DEBUG

# 개발용 DB
spring.datasource.url=jdbc:mysql://localhost:3306/tore_dev
```

**application-prod.properties** (운영 환경, Git에 커밋하지 않음!)
```properties
# 운영 환경 설정
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
logging.level.org.springframework.web=INFO
logging.level.org.hibernate=WARN

# 운영용 DB (환경 변수 사용)
spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
```

**실행 시 프로파일 지정**
```bash
# 개발 환경
java -jar -Dspring.profiles.active=dev app.jar

# 운영 환경
java -jar -Dspring.profiles.active=prod app.jar
```

#### 4.3 Docker Secrets (Docker Swarm)

**docker-compose.yml**
```yaml
version: '3.8'

services:
  app:
    image: toremainserver:latest
    environment:
      SPRING_PROFILES_ACTIVE: prod
    secrets:
      - db_password
      - jwt_secret
    environment:
      DB_PASSWORD_FILE: /run/secrets/db_password
      JWT_SECRET_FILE: /run/secrets/jwt_secret

secrets:
  db_password:
    external: true
  jwt_secret:
    external: true
```

#### 4.4 Kubernetes Secrets

**secrets.yaml**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
type: Opaque
data:
  db-password: <base64-encoded-password>
  jwt-secret: <base64-encoded-secret>
```

**deployment.yaml**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: toremainserver
spec:
  template:
    spec:
      containers:
      - name: app
        image: toremainserver:latest
        env:
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-password
        - name: JWT_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: jwt-secret
```

---

## 5. SQL Injection 방어

### ⚠️ 잠재적 위험

현재 프로젝트는 JPA를 사용하므로 대부분 안전하지만, 동적 쿼리나 네이티브 쿼리 사용 시 주의 필요.

### ✅ 안전한 쿼리 작성

#### 5.1 JPA Query Parameters 사용

```java
// ❌ 위험한 방법
@Query(value = "SELECT * FROM users WHERE username = '" + username + "'", nativeQuery = true)
User findByUsernameDangerous(String username);

// ✅ 안전한 방법
@Query("SELECT u FROM User u WHERE u.username = :username")
Optional<User> findByUsername(@Param("username") String username);

// ✅ 네이티브 쿼리도 파라미터 바인딩 사용
@Query(value = "SELECT * FROM users WHERE username = ?1", nativeQuery = true)
User findByUsernameNative(String username);
```

#### 5.2 Specification 사용 (복잡한 검색)

```java
@Service
public class NFTSearchService {
    
    public List<NFTSellOrder> searchOrders(String seller, 
                                           String minPrice, 
                                           String maxPrice,
                                           String currency) {
        Specification<NFTSellOrder> spec = Specification.where(null);
        
        if (seller != null && !seller.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("seller"), seller)
            );
        }
        
        if (minPrice != null && maxPrice != null) {
            spec = spec.and((root, query, cb) -> 
                cb.between(root.get("price"), minPrice, maxPrice)
            );
        }
        
        if (currency != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("currency"), currency)
            );
        }
        
        return sellOrderRepository.findAll(spec);
    }
}
```

#### 5.3 입력 검증

```java
@Service
public class InputValidator {
    
    /**
     * 지갑 주소 검증 (Ethereum)
     */
    public boolean isValidAddress(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        // Ethereum 주소 형식: 0x로 시작, 40자리 16진수
        return address.matches("^0x[a-fA-F0-9]{40}$");
    }
    
    /**
     * 숫자 문자열 검증
     */
    public boolean isValidNumeric(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return value.matches("^\\d+$");
    }
    
    /**
     * 사용자명 검증
     */
    public boolean isValidUsername(String username) {
        if (username == null || username.isEmpty()) {
            return false;
        }
        // 영문자, 숫자, 언더스코어만 허용, 3-20자
        return username.matches("^[a-zA-Z0-9_]{3,20}$");
    }
}
```

---

## 6. CORS 설정

### ✅ 운영 환경에 맞는 CORS 설정

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed.origins}")
    private String[] allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
    }
}
```

**application.properties**
```properties
# 개발 환경
cors.allowed.origins=http://localhost:3000,http://localhost:8080

# 운영 환경 (환경 변수)
cors.allowed.origins=${CORS_ALLOWED_ORIGINS}
```

---

## 7. 보안 헤더 설정

### ✅ HTTP 보안 헤더 추가

```java
@Configuration
public class SecurityHeadersConfig {
    
    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        // XSS 방어
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Clickjacking 방어
        response.setHeader("X-Frame-Options", "DENY");
        
        // HTTPS 강제 (운영 환경)
        response.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        // CSP (Content Security Policy)
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline';");
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 8. API Rate Limiting

### ✅ Bucket4j를 이용한 Rate Limiting

**의존성 추가**
```gradle
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-core:8.0.1'
implementation 'com.github.vladimir-bukhtoyarov:bucket4j-redis:8.0.1'
```

**Rate Limiting 설정**
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String key = getClientKey(request);
        Bucket bucket = resolveBucket(key);
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write(
                "{\"error\": \"Too many requests. Please try again later.\"}"
            );
        }
    }
    
    private Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }
    
    private Bucket createNewBucket() {
        // 1분당 60회 요청 허용
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }
    
    private String getClientKey(HttpServletRequest request) {
        // IP 주소 기반 (프록시 고려)
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
```

---

## 📝 보안 체크리스트

실제 운영 전 확인해야 할 보안 항목:

- [ ] 비밀번호 BCrypt 해싱 적용
- [ ] Spring Security 활성화 및 설정 완료
- [ ] JWT Secret Key 환경 변수화
- [ ] DB 접속 정보 환경 변수화
- [ ] HTTPS 적용 (Let's Encrypt 등)
- [ ] CORS 설정 (운영 도메인만 허용)
- [ ] Rate Limiting 적용
- [ ] 보안 헤더 설정
- [ ] SQL Injection 방어 확인
- [ ] XSS 방어 확인
- [ ] CSRF 방어 (필요 시)
- [ ] Refresh Token Redis 저장
- [ ] 로그아웃 시 토큰 무효화
- [ ] 민감 정보 로깅 제거
- [ ] 에러 메시지에 민감 정보 노출 확인
- [ ] 파일 업로드 검증 (해당 시)
- [ ] API 문서 접근 제한 (운영 환경)

---

**다음 문서**: [코드 품질 개선 가이드](./CODE_QUALITY_GUIDE.md)

