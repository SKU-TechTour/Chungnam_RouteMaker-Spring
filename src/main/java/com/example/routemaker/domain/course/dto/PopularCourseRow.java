package com.example.routemaker.domain.course.dto;

public record PopularCourseRow(
        String routeKey,
        String region,
        String title,
        String spotsJson,
        int totalDistanceMeters,
        int totalDurationSeconds,
        long bookmarkCount
) {
}
