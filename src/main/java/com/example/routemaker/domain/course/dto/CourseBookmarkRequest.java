package com.example.routemaker.domain.course.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CourseBookmarkRequest(
        @NotBlank String routeKey,
        @NotBlank String region,
        @NotBlank String title,
        @NotEmpty List<@Valid BookmarkSpotRequest> spots,
        int totalDistanceMeters,
        int totalDurationSeconds
) {
}
