package com.example.routemaker.domain.course.dto;

import com.example.routemaker.domain.place.dto.PlaceResponse;

import java.util.List;

public record CongestionAlternativeResponse(
        boolean replacementRecommended,
        double currentRate,
        String currentLevel,
        String reason,
        List<Alternative> alternatives
) {
    public record Alternative(
            PlaceResponse place,
            double congestionRate,
            String congestionLevel,
            int distanceMeters
    ) {
    }
}
