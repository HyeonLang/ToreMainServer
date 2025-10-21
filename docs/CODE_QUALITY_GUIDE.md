# 💎 코드 품질 개선 가이드

> 현업에서 사용하는 실전 코드 품질 개선 방법

---

## 📋 목차

1. [예외 처리 전략](#1-예외-처리-전략)
2. [로깅 가이드](#2-로깅-가이드)
3. [테스트 작성 방법](#3-테스트-작성-방법)
4. [코드 리뷰 체크리스트](#4-코드-리뷰-체크리스트)
5. [리팩토링 패턴](#5-리팩토링-패턴)

---

## 1. 예외 처리 전략

### ⚠️ 현재 문제점

```java
// ❌ 일관되지 않은 예외 처리
if (npcOptional.isEmpty()) {
    return ResponseEntity.badRequest().body(null);
}

// ❌ 너무 광범위한 예외 처리
catch (Exception e) {
    return ResponseEntity.internalServerError().body(errorResponse);
}

// ❌ 중복된 try-catch 패턴
@PostMapping("/api/endpoint1")
public ResponseEntity<?> endpoint1() {
    try {
        // 비즈니스 로직
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

### ✅ 해결 방법

#### 1.1 커스텀 예외 계층 구조

```java
package com.example.toremainserver.exception;

/**
 * 비즈니스 예외 최상위 클래스
 */
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

/**
 * 리소스를 찾을 수 없는 경우
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, Long id) {
        super(ErrorCode.RESOURCE_NOT_FOUND, 
              String.format("%s를 찾을 수 없습니다. ID: %d", resourceName, id));
    }
}

/**
 * 인증 실패
 */
public class InvalidCredentialsException extends BusinessException {
    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS);
    }
}

/**
 * 권한 부족
 */
public class InsufficientPermissionException extends BusinessException {
    public InsufficientPermissionException() {
        super(ErrorCode.INSUFFICIENT_PERMISSION);
    }
}

/**
 * 중복 리소스
 */
public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceName) {
        super(ErrorCode.DUPLICATE_RESOURCE, 
              String.format("이미 존재하는 %s입니다", resourceName));
    }
}

/**
 * 잘못된 입력
 */
public class InvalidInputException extends BusinessException {
    public InvalidInputException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }
}

/**
 * 블록체인 관련 예외
 */
public class BlockchainException extends BusinessException {
    public BlockchainException(String message) {
        super(ErrorCode.BLOCKCHAIN_ERROR, message);
    }
}
```

#### 1.2 에러 코드 정의

```java
package com.example.toremainserver.exception;

import org.springframework.http.HttpStatus;

/**
 * 에러 코드 열거형
 */
public enum ErrorCode {
    // 인증/인가 관련 (4xx)
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH001", "인증 정보가 올바르지 않습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH003", "토큰이 만료되었습니다"),
    INSUFFICIENT_PERMISSION(HttpStatus.FORBIDDEN, "AUTH004", "권한이 부족합니다"),
    
    // 리소스 관련 (4xx)
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RES001", "리소스를 찾을 수 없습니다"),
    DUPLICATE_RESOURCE(HttpStatus.CONFLICT, "RES002", "중복된 리소스입니다"),
    
    // 입력 검증 (4xx)
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "VAL001", "입력 값이 올바르지 않습니다"),
    MISSING_REQUIRED_FIELD(HttpStatus.BAD_REQUEST, "VAL002", "필수 필드가 누락되었습니다"),
    WEAK_PASSWORD(HttpStatus.BAD_REQUEST, "VAL003", "비밀번호가 보안 요구사항을 충족하지 않습니다"),
    
    // NFT 관련 (4xx)
    NFT_ALREADY_MINTED(HttpStatus.BAD_REQUEST, "NFT001", "이미 NFT화된 아이템입니다"),
    NFT_NOT_OWNED(HttpStatus.FORBIDDEN, "NFT002", "소유하지 않은 NFT입니다"),
    
    // 블록체인 관련 (5xx)
    BLOCKCHAIN_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "BC001", "블록체인 서버 오류가 발생했습니다"),
    BLOCKCHAIN_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "BC002", "블록체인 서버 응답 시간 초과"),
    
    // 서버 오류 (5xx)
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS001", "서버 내부 오류가 발생했습니다"),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS002", "데이터베이스 오류가 발생했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "SYS003", "외부 API 호출 오류가 발생했습니다");
    
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}
```

#### 1.3 글로벌 예외 핸들러

```java
package com.example.toremainserver.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 * 모든 Controller에서 발생하는 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        ErrorCode errorCode = ex.getErrorCode();
        
        log.warn("Business exception occurred: {} - {}", 
                errorCode.getCode(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode(errorCode.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(errorResponse);
    }
    
    /**
     * 리소스를 찾을 수 없는 경우
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode(ex.getErrorCode().getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
    }
    
    /**
     * Bean Validation 예외 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? 
                                error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        
        log.warn("Validation failed: {}", fieldErrors);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode(ErrorCode.INVALID_INPUT.getCode())
                .message("입력 값 검증에 실패했습니다")
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .details(fieldErrors)
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    
    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        log.warn("Illegal argument: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode(ErrorCode.INVALID_INPUT.getCode())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
    
    /**
     * 모든 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .success(false)
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .message("서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.")
                .timestamp(LocalDateTime.now())
                .path(extractPath(request))
                .build();
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
    
    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
```

#### 1.4 표준 에러 응답 DTO

```java
package com.example.toremainserver.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 표준 에러 응답 형식
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * 성공 여부 (항상 false)
     */
    private final boolean success;
    
    /**
     * 에러 코드 (예: AUTH001, RES001)
     */
    private final String errorCode;
    
    /**
     * 에러 메시지
     */
    private final String message;
    
    /**
     * 발생 시각
     */
    private final LocalDateTime timestamp;
    
    /**
     * 요청 경로
     */
    private final String path;
    
    /**
     * 추가 상세 정보 (선택적)
     */
    private final Map<String, String> details;
}
```

#### 1.5 Service 계층에서 예외 사용

```java
@Service
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public LoginResponse login(LoginRequest loginRequest) {
        // ✅ 명확한 예외 처리
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException());
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        
        // JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        return new LoginResponse(true, "로그인 성공", accessToken, refreshToken, 3600);
    }
    
    public User registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("사용자명");
        }
        
        // 비밀번호 강도 검증
        passwordValidator.validate(password);
        
        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(username, hashedPassword);
        return userRepository.save(user);
    }
}

@Service
public class NftService {
    
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        // ✅ 리소스 검증
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("사용자", request.getUserId()));
        
        if (user.getWalletAddress() == null || user.getWalletAddress().isEmpty()) {
            throw new InvalidInputException("지갑 주소가 설정되지 않았습니다");
        }
        
        ItemDefinition itemDefinition = itemDefinitionRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("아이템", request.getItemId()));
        
        UserEquipItem userEquipItem = userEquipItemRepository
                .findByUserIdAndItemIdAndLocalItemId(
                        request.getUserId(), 
                        request.getItemId(), 
                        request.getLocalItemId()
                )
                .orElseThrow(() -> new ResourceNotFoundException("장비 아이템", request.getLocalItemId()));
        
        // ✅ 비즈니스 규칙 검증
        if (userEquipItem.getNftId() != null) {
            throw new InvalidInputException("이미 NFT화된 아이템입니다");
        }
        
        // 블록체인 호출
        try {
            ContractNftResponse contractResponse = blockchainClient.mintNft(contractRequest);
            
            if (contractResponse.isSuccess()) {
                userEquipItem.setNftId(contractResponse.getNftId());
                userEquipItemRepository.save(userEquipItem);
                return NftMintClientResponse.success(contractResponse.getNftId());
            } else {
                throw new BlockchainException(contractResponse.getErrorMessage());
            }
        } catch (RestClientException ex) {
            throw new BlockchainException("블록체인 서버 통신 오류: " + ex.getMessage());
        }
    }
}
```

#### 1.6 Controller 간소화

```java
@RestController
@RequestMapping("/api")
public class NftController {
    
    private final NftService nftService;
    
    /**
     * ✅ 예외 처리는 GlobalExceptionHandler에 위임
     * Controller는 비즈니스 로직 호출에만 집중
     */
    @PostMapping("/nft/mint")
    public ResponseEntity<ApiResponse<NftMintClientResponse>> mintNft(
            @Valid @RequestBody NftMintClientRequest request) {
        
        NftMintClientResponse response = nftService.mintNft(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/nft/list/{userId}")
    public ResponseEntity<ApiResponse<NftListClientResponse>> getNftList(
            @PathVariable Long userId) {
        
        NftListClientRequest request = new NftListClientRequest(userId);
        NftListClientResponse response = nftService.getNftList(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
```

---

## 2. 로깅 가이드

### ⚠️ 현재 문제점

```java
// ❌ System.out.println 사용
System.out.println("User logged out: " + username);
System.out.println("Hello");

// ❌ 로그 레벨 미분류
// ❌ 구조화되지 않은 로그
// ❌ 민감 정보 노출 가능성
```

### ✅ 해결 방법

#### 2.1 SLF4J + Logback 설정

**logback-spring.xml** 생성
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    
    <!-- 콘솔 출력 -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 파일 출력 (INFO 레벨 이상) -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxHistory>30</maxHistory>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>10MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 에러 파일 (ERROR 레벨만) -->
    <appender name="ERROR_FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/error.log</file>
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <onMatch>ACCEPT</onMatch>
            <onMismatch>DENY</onMismatch>
        </filter>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/error-%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>90</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    
    <!-- 개발 환경 설정 -->
    <springProfile name="dev">
        <root level="DEBUG">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
        <logger name="com.example.toremainserver" level="DEBUG"/>
        <logger name="org.springframework.web" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
    </springProfile>
    
    <!-- 운영 환경 설정 -->
    <springProfile name="prod">
        <root level="INFO">
            <appender-ref ref="FILE"/>
            <appender-ref ref="ERROR_FILE"/>
        </root>
        <logger name="com.example.toremainserver" level="INFO"/>
        <logger name="org.springframework.web" level="WARN"/>
        <logger name="org.hibernate.SQL" level="WARN"/>
    </springProfile>
    
</configuration>
```

#### 2.2 로깅 사용 예시

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found: {}", loginRequest.getUsername());
                    return new InvalidCredentialsException();
                });
        
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Login failed - invalid password for username: {}", loginRequest.getUsername());
            throw new InvalidCredentialsException();
        }
        
        log.info("Login successful for username: {}", user.getUsername());
        
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        
        return new LoginResponse(true, "로그인 성공", accessToken, refreshToken, 3600);
    }
    
    public void logout(String refreshToken) {
        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            refreshTokenService.deleteRefreshToken(username);
            
            log.info("User logged out successfully: {}", username);
        } else {
            log.warn("Logout attempt with invalid token");
        }
    }
}

@Service
public class NftService {
    
    private static final Logger log = LoggerFactory.getLogger(NftService.class);
    
    @Transactional
    public NftMintClientResponse mintNft(NftMintClientRequest request) {
        log.info("NFT minting started - userId: {}, itemId: {}, localItemId: {}", 
                request.getUserId(), request.getItemId(), request.getLocalItemId());
        
        try {
            // 비즈니스 로직...
            
            ContractNftResponse contractResponse = blockchainClient.mintNft(contractRequest);
            
            if (contractResponse.isSuccess()) {
                log.info("NFT minted successfully - nftId: {}, userId: {}", 
                        contractResponse.getNftId(), request.getUserId());
                
                userEquipItem.setNftId(contractResponse.getNftId());
                userEquipItemRepository.save(userEquipItem);
                
                return NftMintClientResponse.success(contractResponse.getNftId());
            } else {
                log.error("Blockchain minting failed - error: {}", 
                        contractResponse.getErrorMessage());
                throw new BlockchainException(contractResponse.getErrorMessage());
            }
            
        } catch (BlockchainException ex) {
            log.error("NFT minting failed - userId: {}, error: {}", 
                    request.getUserId(), ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            log.error("Unexpected error during NFT minting - userId: {}", 
                    request.getUserId(), ex);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
```

#### 2.3 로그 레벨 가이드

| 레벨 | 사용 시점 | 예시 |
|------|----------|------|
| **TRACE** | 매우 상세한 디버깅 정보 | 메서드 진입/종료, 변수 값 |
| **DEBUG** | 개발 중 디버깅 정보 | 쿼리 실행, API 호출 상세 |
| **INFO** | 주요 이벤트 정보 | 로그인 성공, NFT 민팅 완료 |
| **WARN** | 잠재적 문제 상황 | 로그인 실패, 토큰 만료 |
| **ERROR** | 에러 발생 (복구 가능) | API 호출 실패, DB 쿼리 오류 |
| **FATAL** | 치명적 에러 (시스템 중단) | DB 연결 불가, 필수 설정 누락 |

#### 2.4 민감 정보 로깅 방지

```java
@Service
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    public LoginResponse login(LoginRequest loginRequest) {
        // ❌ 절대 안됨! 비밀번호 로깅
        // log.info("Login attempt: username={}, password={}", 
        //         loginRequest.getUsername(), loginRequest.getPassword());
        
        // ✅ 안전한 로깅
        log.info("Login attempt for username: {}", loginRequest.getUsername());
        
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new InvalidCredentialsException());
        
        // ❌ 절대 안됨! JWT 토큰 전체 로깅
        // log.info("JWT token: {}", accessToken);
        
        // ✅ 안전한 로깅 (토큰 일부만)
        log.debug("JWT token generated for user: {} (token prefix: {}...)", 
                user.getUsername(), accessToken.substring(0, 10));
        
        return new LoginResponse(true, "로그인 성공", accessToken, refreshToken, 3600);
    }
}

/**
 * 민감 정보 마스킹 유틸리티
 */
public class LogMaskingUtil {
    
    /**
     * 이메일 마스킹: test@example.com -> t***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        String[] parts = email.split("@");
        return parts[0].charAt(0) + "***@" + parts[1];
    }
    
    /**
     * 전화번호 마스킹: 010-1234-5678 -> 010-****-5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return "***";
        }
        return phone.substring(0, 3) + "-****-" + phone.substring(phone.length() - 4);
    }
    
    /**
     * 토큰 마스킹: 앞 10자만 표시
     */
    public static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 10) + "...";
    }
}
```

---

## 3. 테스트 작성 방법

### ⚠️ 현재 문제점

```java
// ❌ 빈 테스트
@SpringBootTest
class ToreMainServerApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

### ✅ 해결 방법

#### 3.1 단위 테스트 (Service 계층)

**의존성 추가**
```gradle
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.mockito:mockito-core'
    testImplementation 'org.mockito:mockito-junit-jupiter'
    testImplementation 'com.h2database:h2' // 테스트용 인메모리 DB
}
```

**AuthService 테스트**
```java
package com.example.toremainserver.service;

import com.example.toremainserver.dto.auth.LoginRequest;
import com.example.toremainserver.dto.auth.LoginResponse;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.exception.InvalidCredentialsException;
import com.example.toremainserver.repository.UserRepository;
import com.example.toremainserver.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 테스트")
class AuthServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    
    @InjectMocks
    private AuthService authService;
    
    private User testUser;
    
    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "$2a$10$hashedPassword");
        testUser.setId(1L);
    }
    
    @Test
    @DisplayName("로그인 성공 - 올바른 사용자명과 비밀번호")
    void loginSuccess() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", testUser.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.generateAccessToken("testuser"))
                .thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken("testuser"))
                .thenReturn("refresh-token");
        
        // When
        LoginResponse response = authService.login(request);
        
        // Then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("로그인 성공");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-token");
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void loginFailUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent"))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
        
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void loginFailInvalidPassword() {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpassword", testUser.getPassword()))
                .thenReturn(false);
        
        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
        
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }
    
    @Test
    @DisplayName("회원가입 성공")
    void registerSuccess() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123"))
                .thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(2L);
                    return user;
                });
        
        // When
        User newUser = authService.registerUser("newuser", "password123");
        
        // Then
        assertThat(newUser.getId()).isEqualTo(2L);
        assertThat(newUser.getUsername()).isEqualTo("newuser");
        assertThat(newUser.getPassword()).isEqualTo("$2a$10$hashedPassword");
        
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }
}
```

#### 3.2 통합 테스트 (Controller + Service + Repository)

```java
package com.example.toremainserver.controller;

import com.example.toremainserver.dto.auth.LoginRequest;
import com.example.toremainserver.entity.User;
import com.example.toremainserver.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController 통합 테스트")
class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        User testUser = new User("testuser", passwordEncoder.encode("password123"));
        userRepository.save(testUser);
    }
    
    @Test
    @DisplayName("로그인 성공 - 200 OK")
    void loginSuccess() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "password123");
        
        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }
    
    @Test
    @DisplayName("로그인 실패 - 잘못된 비밀번호 - 400 BAD REQUEST")
    void loginFailInvalidPassword() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("testuser", "wrongpassword");
        
        // When & Then
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    @DisplayName("회원가입 성공 - 201 CREATED")
    void registerSuccess() throws Exception {
        // Given
        LoginRequest request = new LoginRequest("newuser", "password123");
        
        // When & Then
        mockMvc.perform(post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").exists());
    }
}
```

#### 3.3 Repository 테스트

```java
package com.example.toremainserver.repository;

import com.example.toremainserver.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository 테스트")
class UserRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    @DisplayName("사용자명으로 사용자 조회 성공")
    void findByUsernameSuccess() {
        // Given
        User user = new User("testuser", "password123");
        entityManager.persist(user);
        entityManager.flush();
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자명 조회 - Optional.empty()")
    void findByUsernameNotFound() {
        // When
        Optional<User> found = userRepository.findByUsername("nonexistent");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    @DisplayName("사용자명 중복 체크 - true")
    void existsByUsernameTrue() {
        // Given
        User user = new User("testuser", "password123");
        entityManager.persist(user);
        entityManager.flush();
        
        // When
        boolean exists = userRepository.existsByUsername("testuser");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    @DisplayName("사용자명 중복 체크 - false")
    void existsByUsernameFalse() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");
        
        // Then
        assertThat(exists).isFalse();
    }
}
```

#### 3.4 테스트용 application-test.properties

```properties
# 테스트용 H2 인메모리 DB
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT 테스트용 설정
jwt.secret=test-secret-key-for-unit-testing-only-not-for-production
jwt.access-token-validity=3600
jwt.refresh-token-validity=86400

# 로깅
logging.level.org.springframework=INFO
logging.level.com.example.toremainserver=DEBUG
```

---

## 4. 코드 리뷰 체크리스트

### 📝 보안
- [ ] 비밀번호가 해시화되어 저장되는가?
- [ ] JWT 토큰이 안전하게 관리되는가?
- [ ] 민감한 정보(비밀번호, 토큰)가 로그에 출력되지 않는가?
- [ ] 입력 값 검증이 적절히 이루어지는가?
- [ ] SQL Injection 방어가 되어 있는가?
- [ ] CORS 설정이 적절한가?

### 📝 예외 처리
- [ ] 예외가 적절히 처리되고 있는가?
- [ ] 사용자에게 의미 있는 에러 메시지를 제공하는가?
- [ ] 예외 발생 시 적절한 HTTP 상태 코드를 반환하는가?
- [ ] 예외가 글로벌 핸들러에서 처리되는가?

### 📝 로깅
- [ ] 적절한 로그 레벨이 사용되는가?
- [ ] 주요 이벤트가 로깅되는가?
- [ ] System.out.println 대신 Logger를 사용하는가?
- [ ] 민감한 정보가 로깅되지 않는가?

### 📝 코드 품질
- [ ] 메서드가 한 가지 일만 하는가? (Single Responsibility)
- [ ] 중복 코드가 없는가? (DRY)
- [ ] 매직 넘버/문자열이 상수로 정의되어 있는가?
- [ ] 변수/메서드 이름이 의미를 명확히 전달하는가?
- [ ] NULL 체크가 적절히 이루어지는가?

### 📝 성능
- [ ] N+1 쿼리 문제가 없는가?
- [ ] 페이징 처리가 되어 있는가?
- [ ] 적절한 인덱스가 설정되어 있는가?
- [ ] 트랜잭션 범위가 적절한가?

### 📝 테스트
- [ ] 단위 테스트가 작성되어 있는가?
- [ ] 테스트 커버리지가 충분한가? (최소 70%)
- [ ] 통합 테스트가 작성되어 있는가?
- [ ] 엣지 케이스가 테스트되는가?

---

## 5. 리팩토링 패턴

### 5.1 Long Method → Extract Method

**리팩토링 전**
```java
public ResponseEntity<Ue5NpcResponse> forwardNpcRequest(Ue5NpcRequest ue5Request) {
    // 100줄 이상의 코드...
    Optional<Npc> npcOptional = npcRepository.findByNpcId(ue5Request.getNpcId());
    if (npcOptional.isEmpty()) {
        return ResponseEntity.badRequest().body(null);
    }
    Npc npc = npcOptional.get();
    String npcName = npc.getName();
    String npcDescription = "";
    if (npc.getNpcInfo() != null && npc.getNpcInfo().containsKey("description")) {
        npcDescription = (String) npc.getNpcInfo().get("description");
    }
    // ... 더 많은 코드
}
```

**리팩토링 후**
```java
public ResponseEntity<Ue5NpcResponse> forwardNpcRequest(Ue5NpcRequest ue5Request) {
    Npc npc = getNpc(ue5Request.getNpcId());
    User user = getUser(ue5Request.getUserId());
    Conversation conversation = getOrCreateConversation(ue5Request.getUserId(), ue5Request.getNpcId());
    
    NpcChatRequest chatRequest = buildChatRequest(ue5Request, npc, user, conversation);
    NpcChatResponse chatResponse = callAiServer(chatRequest);
    updateConversationHistory(ue5Request, chatResponse);
    
    return ResponseEntity.ok(convertToUe5Response(chatResponse));
}

private Npc getNpc(Long npcId) {
    return npcRepository.findByNpcId(npcId)
            .orElseThrow(() -> new ResourceNotFoundException("NPC", npcId));
}

private User getUser(Long userId) {
    return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
}
```

### 5.2 중복 코드 제거

**리팩토링 전**
```java
// Controller마다 반복
@PostMapping("/endpoint1")
public ResponseEntity<?> endpoint1() {
    try {
        // 로직
    } catch (Exception e) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
}
```

**리팩토링 후**
```java
// 표준 응답 DTO 사용
@PostMapping("/endpoint1")
public ResponseEntity<ApiResponse<ResultDto>> endpoint1() {
    ResultDto result = service.process();
    return ResponseEntity.ok(ApiResponse.success(result));
}

// 예외는 GlobalExceptionHandler에서 처리
```

---

**다음 문서**: [성능 최적화 가이드](./PERFORMANCE_GUIDE.md)

