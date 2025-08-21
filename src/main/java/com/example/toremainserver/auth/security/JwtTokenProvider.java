package com.example.toremainserver.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT 토큰을 생성하고 검증하는 핵심 컴포넌트
 * 
 * 주요 기능:
 * 1. Access Token 생성 (짧은 유효기간 - 1시간)
 * 2. Refresh Token 생성 (긴 유효기간 - 24시간)
 * 3. 토큰 검증 및 파싱
 * 4. 토큰에서 사용자 정보 추출
 */
@Component
public class JwtTokenProvider {
    
    // JWT 서명에 사용할 비밀키 (application.properties에서 설정)
    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;
    
    // Access Token 유효기간 (초 단위, 기본값: 1시간)
    @Value("${jwt.access-token-validity:3600}")
    private long accessTokenValidity;
    
    // Refresh Token 유효기간 (초 단위, 기본값: 24시간)
    @Value("${jwt.refresh-token-validity:86400}")
    private long refreshTokenValidity;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * Access Token 생성
     * 
     * @param username 사용자명
     * @return JWT Access Token (짧은 유효기간)
     * 
     * 사용 시나리오:
     * - 로그인 성공 시 생성
     * - API 요청 시 Authorization 헤더에 포함
     * - 만료되면 Refresh Token으로 갱신 필요
     */
    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenValidity);
    }
    
    /**
     * Refresh Token 생성
     * 
     * @param username 사용자명
     * @return JWT Refresh Token (긴 유효기간)
     * 
     * 사용 시나리오:
     * - 로그인 성공 시 Access Token과 함께 생성
     * - Access Token 만료 시 새로운 Access Token 발급에 사용
     * - 보안을 위해 안전한 곳에 저장 필요
     */
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenValidity);
    }
    
    /**
     * JWT 토큰 생성 (내부 메서드)
     * 
     * @param username 사용자명
     * @param validity 토큰 유효기간 (초)
     * @return 생성된 JWT 토큰
     * 
     * 토큰 구조:
     * - Header: 알고리즘 정보 (HS256)
     * - Payload: 사용자명, 발급시간, 만료시간
     * - Signature: 비밀키로 서명
     */
    private String generateToken(String username, long validity) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username, validity);
    }
    
    /**
     * JWT 토큰 생성 (실제 구현)
     * 
     * @param claims 추가 클레임 정보 (현재는 비어있음)
     * @param subject 토큰 주체 (사용자명)
     * @param validity 토큰 유효기간 (초)
     * @return 서명된 JWT 토큰 문자열
     * 
     * 생성 과정:
     * 1. 현재 시간과 만료 시간 설정
     * 2. JWT Builder로 토큰 구성
     * 3. HS256 알고리즘으로 서명
     * 4. Base64 인코딩된 문자열 반환
     */
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validity * 1000);
        
        return Jwts.builder()
                .setClaims(claims)                    // 추가 클레임 (현재는 비어있음)
                .setSubject(subject)                  // 토큰 주체 (사용자명)
                .setIssuedAt(now)                     // 발급 시간
                .setExpiration(expiryDate)            // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 서명
                .compact();                           // 문자열로 압축
    }
    
    /**
     * JWT 토큰에서 사용자명 추출
     * 
     * @param token JWT 토큰
     * @return 토큰에 포함된 사용자명
     * 
     * 사용 시나리오:
     * - API 요청 시 토큰에서 사용자 식별
     * - 토큰 갱신 시 사용자 확인
     * - 로그인 상태 확인
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * JWT 토큰에서 만료일 추출
     * 
     * @param token JWT 토큰
     * @return 토큰 만료 시간
     * 
     * 사용 시나리오:
     * - 토큰 만료 여부 확인
     * - 토큰 유효성 검증
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * JWT 토큰에서 특정 클레임 추출
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * JWT 토큰에서 모든 클레임 추출
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * JWT 토큰 만료 여부 확인
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * JWT 토큰 검증 (UserDetails와 함께)
     * 
     * @param token JWT 토큰
     * @param userDetails 사용자 상세 정보
     * @return 토큰 유효성 여부
     * 
     * 검증 과정:
     * 1. 토큰에서 사용자명 추출
     * 2. UserDetails의 사용자명과 비교
     * 3. 토큰 만료 여부 확인
     * 
     * 사용 시나리오:
     * - JwtAuthenticationFilter에서 요청 시 검증
     * - 사용자 인증 상태 확인
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    /**
     * JWT 토큰 검증 (토큰만으로)
     * 
     * @param token JWT 토큰
     * @return 토큰 유효성 여부
     * 
     * 검증 과정:
     * 1. 토큰 서명 검증 (위조 방지)
     * 2. 토큰 만료 여부 확인
     * 3. 토큰 형식 검증
     * 
     * 사용 시나리오:
     * - Refresh Token 검증
     * - 토큰 갱신 전 유효성 확인
     * - 로그아웃 시 토큰 검증
     */
    public Boolean validateToken(String token) {
        try {
            // 토큰 파싱 및 서명 검증
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            
            // 토큰 만료 여부 확인
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            // 토큰이 유효하지 않거나 만료된 경우
            return false;
        }
    }
}
