package com.example.routemaker.domain.course.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RouteLegResponse {
    private Long originPlaceId;
    private Long destinationPlaceId;
    private int distanceMeters;
    private int durationSeconds;
    private int tollWon;
    private int taxiFareWon;
    private String source;
}
