package com.example.routemaker.global.util;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    // TODO: JWT secret 설정 및 토큰 생성/검증 로직 구현

    public String extractEmail(String token) {
        return null;
    }

    public boolean isTokenValid(String token, org.springframework.security.core.userdetails.UserDetails userDetails) {
        return false;
    }

    public String generateToken(String email) {
        return "";
    }
}
