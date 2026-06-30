package com.example.routemaker.domain.user.auth.controller;

import com.example.routemaker.domain.user.auth.dto.LoginRequest;
import com.example.routemaker.domain.user.auth.dto.SignUpRequest;
import com.example.routemaker.domain.user.auth.dto.TokenResponse;
import com.example.routemaker.domain.user.auth.service.AuthService;
import com.example.routemaker.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ApiResponse<Void> signUp(@Valid @RequestBody SignUpRequest request) {
        authService.signUp(request);
        return ApiResponse.success("회원가입이 완료되었습니다.", null);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }
}
