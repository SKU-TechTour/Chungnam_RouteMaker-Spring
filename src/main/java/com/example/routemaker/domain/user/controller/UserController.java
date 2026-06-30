package com.example.routemaker.domain.user.controller;

import com.example.routemaker.domain.user.dto.MyPageResponse;
import com.example.routemaker.domain.user.dto.UserUpdateRequest;
import com.example.routemaker.domain.user.service.UserService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<MyPageResponse> getMyPage(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(userService.getMyPage(userDetails.getUsername()));
    }

    @PutMapping("/me")
    public ApiResponse<MyPageResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest request
    ) {
        return ApiResponse.success(userService.updateProfile(userDetails.getUsername(), request));
    }
}
