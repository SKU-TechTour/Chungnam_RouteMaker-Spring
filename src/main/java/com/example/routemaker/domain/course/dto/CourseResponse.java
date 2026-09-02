package com.example.routemaker.domain.course.dto;

import com.example.routemaker.domain.place.dto.PlaceResponse;
import com.example.routemaker.global.common.enums.Region;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private Region region;
    private boolean indoor;
    private String weather;
    private List<HourlyWeatherResponse> hourlyWeather;
    private List<PlaceResponse> combo;
    private List<RouteLegResponse> routes;
    private int totalDistanceMeters;
    private int totalDurationSeconds;
    private String source;
}
