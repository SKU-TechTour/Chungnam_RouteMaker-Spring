package com.example.routemaker.domain.course.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
    private List<RouteCoordinateResponse> path;
    private List<RouteGuideResponse> guides;
}
