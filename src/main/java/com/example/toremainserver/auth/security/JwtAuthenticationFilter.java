package com.example.toremainserver.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 * 
 * 모든 HTTP 요청을 가로채서 JWT 토큰을 검증하고 인증을 처리합니다.
 * 
 * 동작 흐름:
 * 1. 요청에서 Authorization 헤더 추출
 * 2. "Bearer " 접두사 확인 후 JWT 토큰 추출
 * 3. 토큰에서 사용자명 추출
 * 4. 사용자 정보 로드 및 토큰 검증
 * 5. SecurityContext에 인증 정보 설정
 * 
 * 필터 체인에서 UsernamePasswordAuthenticationFilter 이전에 실행됩니다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    // JWT 토큰 생성/검증 담당
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    // 사용자 정보 로드 담당
    @Autowired
    private UserDetailsService userDetailsService;
    
    /**
     * HTTP 요청을 가로채서 JWT 토큰을 검증하고 인증을 처리합니다.
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * 
     * 처리 과정:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. 토큰에서 사용자명 추출
     * 3. 사용자 정보 로드 및 토큰 검증
     * 4. SecurityContext에 인증 정보 설정
     * 5. 다음 필터로 요청 전달
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 1단계: Authorization 헤더에서 JWT 토큰 추출
        final String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        // "Bearer " 접두사가 있는지 확인하고 JWT 토큰 추출
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // "Bearer " 제거 (7글자)
            try {
                // JWT 토큰에서 사용자명 추출
                username = jwtTokenProvider.getUsernameFromToken(jwt);
            } catch (Exception e) {
                // 토큰 파싱 실패 시 로그 기록
                logger.error("JWT 토큰에서 사용자명 추출 실패", e);
            }
        }
        
        // 2단계: 사용자 인증 처리
        // 사용자명이 추출되었고 현재 SecurityContext에 인증 정보가 없는 경우
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 데이터베이스에서 사용자 정보 로드
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            // JWT 토큰 검증 (토큰 유효성 + 사용자 정보 일치 확인)
            if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                // 인증 토큰 생성 (Spring Security용)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                
                // 요청 상세 정보 설정
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // SecurityContext에 인증 정보 설정 (이후 요청에서 사용자 정보 접근 가능)
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        // 3단계: 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }
}
