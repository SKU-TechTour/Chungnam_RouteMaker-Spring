package com.example.routemaker.domain.course.dto;

public record BookmarkSpotRequest(
        String id,
        String name,
        String category,
        double latitude,
        double longitude,
        String imageUrl,
        String source,
        String address,
        String scheduledTime
) {
}
