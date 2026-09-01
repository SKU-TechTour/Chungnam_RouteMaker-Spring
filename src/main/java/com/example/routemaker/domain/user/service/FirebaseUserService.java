package com.example.routemaker.domain.user.service;

import com.example.routemaker.domain.user.entity.User;
import com.example.routemaker.domain.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FirebaseUserService {

    private final UserRepository userRepository;

    @Transactional
    public User findOrCreate(FirebaseToken token) {
        return userRepository.findByFirebaseUid(token.getUid())
                .orElseGet(() -> linkExistingOrCreate(token));
    }

    private User linkExistingOrCreate(FirebaseToken token) {
        String email = StringUtils.hasText(token.getEmail())
                ? token.getEmail()
                : token.getUid() + "@anonymous.firebase.local";

        return userRepository.findByEmail(email)
                .map(user -> {
                    user.linkFirebaseUid(token.getUid());
                    return user;
                })
                .orElseGet(() -> userRepository.save(User.builder()
                        .firebaseUid(token.getUid())
                        .email(email)
                        .password("{firebase}")
                        .nickname(resolveNickname(token))
                        .military(false)
                        .build()));
    }

    private String resolveNickname(FirebaseToken token) {
        Object name = token.getClaims().get("name");
        return name instanceof String value && StringUtils.hasText(value)
                ? value
                : "여행자";
    }
}
