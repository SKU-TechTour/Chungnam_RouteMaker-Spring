package com.example.routemaker.domain.military.controller;

import com.example.routemaker.domain.military.dto.EnlistmentScheduleResponse;
import com.example.routemaker.domain.military.dto.SafeTimeResponse;
import com.example.routemaker.domain.military.service.EnlistmentScheduleService;
import com.example.routemaker.domain.military.service.SafeTimeService;
import com.example.routemaker.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/military")
@RequiredArgsConstructor
public class MilitaryController {

    private final SafeTimeService safeTimeService;
    private final EnlistmentScheduleService enlistmentScheduleService;

    @GetMapping("/enlistment-schedules")
    public ApiResponse<List<EnlistmentScheduleResponse>> getEnlistmentSchedules(
            @RequestParam(required = false) String branch,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate
    ) {
        return ApiResponse.success(enlistmentScheduleService.getSchedules(branch, fromDate, toDate));
    }

    @GetMapping("/enlistment-schedules/{scheduleId}")
    public ApiResponse<EnlistmentScheduleResponse> getEnlistmentSchedule(@PathVariable Long scheduleId) {
        return ApiResponse.success(enlistmentScheduleService.getSchedule(scheduleId));
    }

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
