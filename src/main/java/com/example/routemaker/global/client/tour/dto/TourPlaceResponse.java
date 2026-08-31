package com.example.routemaker.global.client.tour.dto;

import tools.jackson.databind.JsonNode;

public record TourPlaceResponse(
        String contentId,
        String contentTypeId,
        String name,
        String address,
        String imageUrl,
        double longitude,
        double latitude
) {
    public static TourPlaceResponse from(JsonNode item) {
        return new TourPlaceResponse(
                item.path("contentid").asText(), item.path("contenttypeid").asText(),
                item.path("title").asText(), item.path("addr1").asText(),
                item.path("firstimage").asText(), item.path("mapx").asDouble(),
                item.path("mapy").asDouble()
        );
    }
}
