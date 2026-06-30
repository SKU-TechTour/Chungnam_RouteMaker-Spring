package com.example.routemaker.domain.user.auth.service;

import com.example.routemaker.domain.user.auth.dto.LoginRequest;
import com.example.routemaker.domain.user.auth.dto.SignUpRequest;
import com.example.routemaker.domain.user.auth.dto.TokenResponse;
import com.example.routemaker.domain.user.entity.User;
import com.example.routemaker.domain.user.repository.UserRepository;
import com.example.routemaker.global.exception.BusinessException;
import com.example.routemaker.global.exception.ErrorCode;
import com.example.routemaker.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signUp(SignUpRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .military(request.isMilitary())
                .trainingStartDate(request.getTrainingStartDate())
                .militaryUnitId(request.getMilitaryUnitId())
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        return new TokenResponse(accessToken, "Bearer");
    }
}
