package com.example.routemaker.domain.course.dto;

import java.util.List;

public record RoutePreviewResponse(
        List<RouteLegResponse> routes,
        int totalDistanceMeters,
        int totalDurationSeconds,
        String source
) {
}
