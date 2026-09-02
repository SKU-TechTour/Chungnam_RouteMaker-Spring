package com.example.routemaker.domain.course.dto;

public record HourlyWeatherResponse(
        String time,
        int temperature,
        int precipitationProbability,
        boolean precipitationExpected
) {
}
