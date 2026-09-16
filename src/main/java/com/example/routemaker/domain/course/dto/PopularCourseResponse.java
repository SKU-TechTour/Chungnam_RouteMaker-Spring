package com.example.routemaker.domain.course.dto;

import java.util.List;

public record PopularCourseResponse(
        String routeKey,
        String region,
        String title,
        List<BookmarkSpotRequest> spots,
        int totalDistanceMeters,
        int totalDurationSeconds,
        long bookmarkCount,
        double regionVisitorCount,
        double popularityScore,
        String rankingBasis
) {
}
