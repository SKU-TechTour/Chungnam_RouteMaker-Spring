package com.example.routemaker.global.client.weather.dto;

import tools.jackson.databind.JsonNode;

public record WeatherForecastItemResponse(
        String category,
        String forecastDate,
        String forecastTime,
        String value
) {
    public static WeatherForecastItemResponse from(JsonNode item) {
        return new WeatherForecastItemResponse(item.path("category").asText(),
                item.path("fcstDate").asText(), item.path("fcstTime").asText(),
                item.path("fcstValue").asText());
    }
}
