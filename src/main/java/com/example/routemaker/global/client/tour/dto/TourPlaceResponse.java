package com.example.routemaker.global.client.tour.dto;

import tools.jackson.databind.JsonNode;

public record TourPlaceResponse(
        String contentId,
        String contentTypeId,
        String categoryCode,
        String name,
        String address,
        String imageUrl,
        double longitude,
        double latitude
) {
    public static TourPlaceResponse from(JsonNode item) {
        return new TourPlaceResponse(
                item.path("contentid").asText(), item.path("contenttypeid").asText(),
                item.path("cat3").asText(),
                item.path("title").asText(), item.path("addr1").asText(),
                secureImageUrl(item.path("firstimage").asText()), item.path("mapx").asDouble(),
                item.path("mapy").asDouble()
        );
    }

    private static String secureImageUrl(String imageUrl) {
        return imageUrl.startsWith("http://")
                ? "https://" + imageUrl.substring("http://".length())
                : imageUrl;
    }
}
