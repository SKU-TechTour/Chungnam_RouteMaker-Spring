package com.example.routemaker.domain.course.dto;

public record RouteGuideResponse(
        String instruction,
        double latitude,
        double longitude,
        int distanceMeters,
        int durationSeconds
) {
}
