package com.example.routemaker.domain.reward.controller;

import com.example.routemaker.domain.reward.dto.ShareCardResponse;
import com.example.routemaker.domain.reward.dto.StampResponse;
import com.example.routemaker.domain.reward.service.RewardService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final RewardService rewardService;

    @PostMapping("/stamps/{stampId}")
    public ApiResponse<StampResponse> grantStamp(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long stampId
    ) {
        return ApiResponse.success(rewardService.grantStamp(userDetails.getUsername(), stampId));
    }

    @PostMapping("/share-cards/{courseId}")
    public ApiResponse<ShareCardResponse> createShareCard(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long courseId
    ) {
        return ApiResponse.success(rewardService.createShareCard(userDetails.getUsername(), courseId));
    }
}
