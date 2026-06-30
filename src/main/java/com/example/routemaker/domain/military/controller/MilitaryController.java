package com.example.routemaker.domain.military.controller;

import com.example.routemaker.domain.military.dto.SafeTimeResponse;
import com.example.routemaker.domain.military.service.SafeTimeService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/military")
@RequiredArgsConstructor
public class MilitaryController {

    private final SafeTimeService safeTimeService;

    @GetMapping("/safe-time")
    public ApiResponse<SafeTimeResponse> getSafeTime(
            @RequestParam Long unitId,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam int returnDeadlineMinutes
    ) {
        return ApiResponse.success(safeTimeService.calculateSafeTime(unitId, lat, lng, returnDeadlineMinutes));
    }
}
